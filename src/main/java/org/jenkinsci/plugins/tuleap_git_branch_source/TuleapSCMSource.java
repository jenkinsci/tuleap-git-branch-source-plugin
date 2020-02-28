package org.jenkinsci.plugins.tuleap_git_branch_source;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.plugins.git.GitSCMBuilder;
import jenkins.plugins.git.traits.RefSpecsSCMSourceTrait;
import jenkins.scm.api.*;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.api.trait.SCMSourceTrait;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.tuleap_credentials.TuleapAccessToken;
import org.jenkinsci.plugins.tuleap_git_branch_source.client.TuleapClientCommandConfigurer;
import org.jenkinsci.plugins.tuleap_git_branch_source.client.TuleapClientRawCmd;
import org.jenkinsci.plugins.tuleap_git_branch_source.client.api.TuleapBranches;
import org.jenkinsci.plugins.tuleap_git_branch_source.client.api.TuleapFileContent;
import org.jenkinsci.plugins.tuleap_git_branch_source.client.api.TuleapGitRepository;
import org.jenkinsci.plugins.tuleap_git_branch_source.client.api.TuleapProject;
import org.jenkinsci.plugins.tuleap_git_branch_source.config.TuleapConfiguration;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jenkinsci.plugins.tuleap_git_branch_source.config.TuleapConnector.*;

/**
 * SCM source implementation for Tuleap discover branch af a repo
 */
public class TuleapSCMSource extends AbstractGitSCMSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TuleapSCMSource.class);

    /**
     * Project Id of the source to be manipulated
     */
    private String projectId;

    /**
     * Tuleap repository to be manipulated (build URL from, build repoId, ...).
     */
    private TuleapGitRepository repository;

    private TuleapProject project;

    /**
     * Git Repository of the source to be manipulated
     */
    private String repositoryPath;

    /**
     * Git remote URL.
     */
    private String remoteUrl;

    /**
     * The behaviours to apply to this source.
     */
    private List<SCMSourceTrait> traits = new ArrayList<>();
    private String credentialsId;
    private TuleapAccessToken credentials;

    @DataBoundConstructor
    public TuleapSCMSource(TuleapProject project, TuleapGitRepository repository) {
        this.repository = repository;
        this.project = project;
        this.projectId = String.valueOf(project.getId());
        this.repositoryPath = repository.getPath();
    }

    @NonNull
    @Override
    protected List<Action> retrieveActions(@NonNull SCMHead head, @CheckForNull SCMHeadEvent event,
        @NonNull TaskListener listener) throws IOException, InterruptedException {
        List<Action> result = new ArrayList<>();
        SCMSourceOwner owner = getOwner();
        if (owner instanceof Actionable) {
            TuleapLink repoLink = ((Actionable) owner).getAction(TuleapLink.class);
            if (repoLink != null) {
                String canonicalRepoName = repositoryPath.replace(project.getShortname()+"/", "");
                String url = repoLink.getUrl() + "?p=" + canonicalRepoName + "&a=shortlog&h=" + head.getName();
                result.add(new TuleapLink("icon-git-branch", url));
            }
        }
        return result;
    }

    @NonNull
    @Override
    protected List<Action> retrieveActions(@CheckForNull SCMSourceEvent event, @NonNull TaskListener listener)
        throws IOException, InterruptedException {
        List<Action> result = new ArrayList<>();
        result.add(new TuleapLink("icon-git-repo", getGitBaseUri() + repositoryPath.replace(".git", "")));
        return result;
    }

    @Override
    protected void retrieve(@CheckForNull SCMSourceCriteria criteria, @NonNull SCMHeadObserver observer,
        @CheckForNull SCMHeadEvent<?> event, @NonNull TaskListener listener) throws IOException, InterruptedException {
        try (final TuleapSCMSourceRequest request = new TuleapSCMSourceContext(criteria, observer)
                .withTraits(traits).wantBranches(true)
                .newRequest(this, listener)) {
            TuleapAccessToken credentials = lookupScanCredentials((Item) getOwner(), getApiBaseUri(),
                getCredentialsId());
            setCredentials(credentials);
            setRemoteUrl(getGitBaseUri() + repositoryPath);
            if (request.isFetchBranches()) {
                LOGGER.info("Fecthing branches for repository at {}", repositoryPath);
                Stream<TuleapBranches> branches = TuleapClientCommandConfigurer.<Stream<TuleapBranches>>newInstance(getApiBaseUri())
                    .withCredentials(credentials)
                    .withCommand(new TuleapClientRawCmd.Branches(this.repository.getId()))
                    .configure()
                    .call();
                request.setBranchesFromTuleapApi(branches);
                int count = 0;
                for (TuleapBranches branch : branches.collect(Collectors.toList())) {
                    count++;
                    request.listener().getLogger().println("Get the Jenkinsfile from Tuleap.");
                    Optional<TuleapFileContent> file = TuleapClientCommandConfigurer.<Optional<TuleapFileContent>>newInstance(getApiBaseUri())
                        .withCredentials(credentials)
                        .withCommand(new TuleapClientRawCmd.GetJenkinsFile(repository.getId(), "Jenkinsfile", branch.getName()))
                        .configure()
                        .call();
                    if (file.get().getName() != null) {
                        request.listener().getLogger().format("Search at '%s'", branch.getName());
                        TuleapBranchSCMHead head = new TuleapBranchSCMHead(branch.getName());
                        if (request.process(head, new SCMRevisionImpl(head, branch.getCommit().getId()),
                            TuleapSCMSource.this::fromSCMFileSystem, new OFWitness(listener))) {
                            request.listener().getLogger()
                                .format("%n  %d branches were processed (query completed)%n", count).println();
                        }
                    } else {
                        request.listener().getLogger().format("There is no Jenkinsfile at the branch: %s %n", branch.getName());
                    }

                }

            }
        }
    }

    @Override
    protected SCMRevision retrieve(SCMHead head, TaskListener listener) throws IOException, InterruptedException {
        Optional<String> revision = Optional.empty();
        Stream<TuleapBranches> branches = TuleapClientCommandConfigurer.<Stream<TuleapBranches>>newInstance(getApiBaseUri())
            .withCredentials(credentials)
            .withCommand(new TuleapClientRawCmd.Branches(this.repository.getId()))
            .configure()
            .call();
        Optional<TuleapBranches> branch = branches.filter(b -> b.getName().equals(head.getName()))
                                                   .findFirst();
        if (branch.isPresent()) {
            revision = Optional.of(branch.get().getCommit().getId());
        } else {
            listener.getLogger().format("Cannot find the branch %s in repo : %s", head.getName(), repositoryPath);
        }
        if (revision.isPresent()) {
            return new SCMRevisionImpl(head, revision.get());
        } else {
            listener.getLogger().format("Cannot resolve the hash of the revision in branch %s%n", head.getName());
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isCategoryEnabled(@NonNull SCMHeadCategory category) {
        if (super.isCategoryEnabled(category)) {
            for (SCMSourceTrait trait : traits) {
                if (trait.isCategoryEnabled(category)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setCredentials(TuleapAccessToken credentials) {
        this.credentials = credentials;
    }

    @Override
    public SCM build(@NonNull SCMHead scmHead, @CheckForNull SCMRevision scmRevision) {
        return new GitSCMBuilder(scmHead, scmRevision, remoteUrl, credentialsId).withTraits(traits).build();
    }

    public List<SCMSourceTrait> getTraits() {
        return Collections.unmodifiableList(traits);
    }

    @DataBoundSetter
    public void setTraits(List<SCMSourceTrait> traits) {
        this.traits = new ArrayList<>(Util.fixNull(traits));
    }

    @Override
    public String getRemote() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    /**
     * Gets the credentials used to access the Tuleap REST API (also used as the default credentials for checking
     * out sources.
     *
     * @return the credentials used to access the Tuleap REST API
     */
    @Override
    @CheckForNull
    public String getCredentialsId() {
        return credentialsId;
    }

    /**
     * Sets the credentials used to access the Tuleap REST API (also used as the default credentials for checking
     * out sources.
     *
     * @param credentialsId
     *            the credentials used to access the Tuleap REST API
     * @since 2.2.0
     */
    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    /**
     * Gets the Id of the project who's repositories will be navigated.
     *
     * @return the Idof the project who's repositories will be navigated.
     */
    public String getprojectId() {
        return projectId;
    }

    @DataBoundSetter
    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    @DataBoundSetter
    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public String getApiBaseUri() {
        return TuleapConfiguration.get().getApiBaseUrl();
    }

    public String getGitBaseUri() {
        return TuleapConfiguration.get().getGitBaseUrl();
    }

    @Symbol("Tuleap")
    @Extension
    public static class DescriptorImpl extends SCMSourceDescriptor {

        @Override
        public String getDisplayName() {
            return "Tuleap";
        }

        public List<SCMSourceTrait> getTraitsDefaults() {
            return Arrays.asList(new RefSpecsSCMSourceTrait());
        }

        @RequirePOST
        @Restricted(NoExternalUse.class) // stapler
        public FormValidation doCheckCredentialsId(@AncestorInPath Item item, @QueryParameter String apiUri,
            @QueryParameter String credentialsId) {

            return checkCredentials(item, apiUri, credentialsId);
        }

        public ListBoxModel doFillCredentialsIdItems(@CheckForNull @AncestorInPath Item context,
            @QueryParameter String apiUri, @QueryParameter String credentialsId) {
            return listScanCredentials(context, apiUri, credentialsId, false);
        }

        @Restricted(NoExternalUse.class) // stapler
        @SuppressWarnings("unused") // stapler
        public ListBoxModel doFillProjectIdItems(@CheckForNull @AncestorInPath Item context,
            @QueryParameter String projectId, @QueryParameter String credentialsId) throws IOException {
            String apiUri = TuleapConfiguration.get().getApiBaseUrl();
            final TuleapAccessToken credentials = lookupScanCredentials(context, apiUri, credentialsId);
            ListBoxModel result = new ListBoxModel();
            Optional<TuleapProject> project = TuleapClientCommandConfigurer.<Optional<TuleapProject>> newInstance(apiUri)
                .withCredentials(credentials)
                .withCommand(new TuleapClientRawCmd.ProjectById(projectId))
                .configure()
                .call();
            if (project.isPresent()) {
                ListBoxModel.Option newItem = new ListBoxModel.Option(project.get().getShortname(),
                    String.valueOf(project.get().getId()));
                result.add(newItem);
            }
            return result;
        }

        @Restricted(NoExternalUse.class) // stapler
        public ListBoxModel doFillRepositoryPathItems(@CheckForNull @AncestorInPath Item context,
            @QueryParameter String projectId, @QueryParameter String credentialsId,
            @QueryParameter String repositoryPath) throws IOException {
            ListBoxModel result = new ListBoxModel();
            final String apiBaseUrl = TuleapConfiguration.get().getApiBaseUrl();
            TuleapAccessToken credentials = lookupScanCredentials(context, apiBaseUrl, credentialsId);
            Optional<TuleapGitRepository> repo = TuleapClientCommandConfigurer
                .<Stream<TuleapGitRepository>>newInstance(apiBaseUrl)
                .withCredentials(credentials)
                .withCommand(new TuleapClientRawCmd.AllRepositoriesByProject(projectId))
                .configure()
                .call()
                .distinct().filter(r -> r.getPath().equals(repositoryPath)).findFirst();
            if (repo.isPresent()) {
                final ListBoxModel.Option newItem = new ListBoxModel.Option(repo.get().getName(), repo.get().getPath());
                result.add(newItem);
            }
            return result;
        }
    }

    private static class OFWitness implements SCMSourceRequest.Witness {
        private final TaskListener listener;

        public OFWitness(TaskListener listener) {
            this.listener = listener;
        }

        @Override
        public void record(@NonNull SCMHead scmHead, @CheckForNull SCMRevision revision, boolean isMatch) {
            if (isMatch) {
                listener.getLogger().format("    Met criteria%n");
            } else {
                listener.getLogger().format("    Does not meet criteria%n");
            }
        }
    }

}
