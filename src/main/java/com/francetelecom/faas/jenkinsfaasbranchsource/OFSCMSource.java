package com.francetelecom.faas.jenkinsfaasbranchsource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.transport.RefSpec;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.TuleapClientCommandConfigurer;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.TuleapClientRawCmd;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapGitBranch;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapGitRepository;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapProject;
import com.francetelecom.faas.jenkinsfaasbranchsource.config.TuleapConfiguration;
import com.francetelecom.faas.jenkinsfaasbranchsource.trait.BranchDiscoveryTrait;

import static com.francetelecom.faas.jenkinsfaasbranchsource.config.TuleapConnector.checkCredentials;
import static com.francetelecom.faas.jenkinsfaasbranchsource.config.TuleapConnector.listScanCredentials;
import static com.francetelecom.faas.jenkinsfaasbranchsource.config.TuleapConnector.lookupScanCredentials;

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
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.SCMSourceEvent;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.api.trait.SCMSourceTrait;

/**
 * SCM source implementation for OrangeForge discover branch af a repo
 */
public class OFSCMSource extends AbstractGitSCMSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OFSCMSource.class);

    /**
     * OrangeForge project to build URL from.
     */
    private String projectId;

    private String apiBaseUri;
    private String gitBaseUri;

    /**
     * Git Repository path to build URL from.
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
    private StandardCredentials credentials;

    @DataBoundConstructor
    public OFSCMSource(final String projectId, final String repositoryPath) {
        this.projectId = projectId;
        this.repositoryPath = repositoryPath;
        traits.add(new BranchDiscoveryTrait());
    }

    @NonNull
    @Override
    protected List<Action> retrieveActions(@NonNull SCMHead head, @CheckForNull SCMHeadEvent event,
        @NonNull TaskListener listener) throws IOException, InterruptedException {
        List<Action> result = new ArrayList<>();
        SCMSourceOwner owner = getOwner();
        if (owner instanceof Actionable) {
            OFLink repoLink = ((Actionable) owner).getAction(OFLink.class);
            if (repoLink != null) {
                String canonicalRepoName = repositoryPath.replace("faas/", "");
                String url = repoLink.getUrl() + "?p=" + canonicalRepoName + "&a=shortlog&h=" + head.getName();
                result.add(new OFLink("icon-git-branch", url));
            }
        }
        return result;
    }

    @NonNull
    @Override
    protected List<Action> retrieveActions(@CheckForNull SCMSourceEvent event, @NonNull TaskListener listener)
        throws IOException, InterruptedException {
        List<Action> result = new ArrayList<>();
        result.add(new OFLink("icon-git-repo", getGitBaseUri() + repositoryPath.replace(".git", "")));
        return result;
    }

    @Override
    protected void retrieve(@CheckForNull SCMSourceCriteria criteria, @NonNull SCMHeadObserver observer,
        @CheckForNull SCMHeadEvent<?> event, @NonNull TaskListener listener) throws IOException, InterruptedException {
        try (final OFSCMSourceRequest request = new OFSCMSourceContext(criteria, observer)
                .withTraits(traits).wantBranches(true)
                .newRequest(this, listener)) {
            StandardCredentials credentials = lookupScanCredentials((Item) getOwner(), getApiBaseUri(),
                getCredentialsId());
            setCredentials(credentials);
            setRemoteUrl(getGitBaseUri() + repositoryPath);
            if (request.isFetchBranches()) {
                LOGGER.info("Fecthing branches for repository at {}", repositoryPath);
                final TuleapClientRawCmd.Command<List<TuleapGitBranch>> allBranchesByGitRepo = new TuleapClientRawCmd().new AllBranchesByGitRepo(
                    repositoryPath);
                TuleapClientRawCmd.Command<List<TuleapGitBranch>> configuredCmd = TuleapClientCommandConfigurer
                        .<List<TuleapGitBranch>> newInstance(getApiBaseUri())
                        .withCredentials(credentials).withGitUrl(getGitBaseUri()).withCommand(allBranchesByGitRepo)
                        .configure();
                List<TuleapGitBranch> branches = configuredCmd.call();
                request.setBranches(branches);
                int count = 0;
                for (TuleapGitBranch branch : branches) {
                    count++;
                    request.listener().getLogger()
                        .format("Crawling branch %s::%s for repo %s", branch.getName(), branch.getSha1(), getRemote())
                        .println();
                    OFBranchSCMHead head = new OFBranchSCMHead(branch.getName());
                    if (request.process(head, new SCMRevisionImpl(head, branch.getSha1()),
                        OFSCMSource.this::fromSCMFileSystem, new OFWitness(listener))) {
                        request.listener().getLogger()
                            .format("%n  %d branches were processed (query completed)%n", count).println();
                    }

                }

            }
        }
    }

    @Override
    protected List<RefSpec> getRefSpecs() {
        return Arrays.asList(new RefSpec("+refs/heads/*:refs/remotes/origin/*", RefSpec.WildcardMode.ALLOW_MISMATCH));
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

    public void setCredentials(StandardCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public SCM build(@NonNull SCMHead scmHead, @CheckForNull SCMRevision scmRevision) {
        // TODO check credentialsId is propagated from Navigator to here and to GtiSCM so it can perform clone
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
     * Gets the credentials used to access the OrangeForge REST API (also used as the default credentials for checking
     * out sources.
     *
     * @return the credentials used to access the OrangeForge REST API
     */
    @Override
    @CheckForNull
    public String getCredentialsId() {
        return credentialsId;
    }

    /**
     * Sets the credentials used to access the OrangeForge REST API (also used as the default credentials for checking
     * out sources.
     *
     * @param credentialsId
     *            the credentials used to access the OrangeForge REST API
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
        if (StringUtils.isBlank(apiBaseUri)) {
            apiBaseUri = TuleapConfiguration.get().getApiBaseUrl();
        }
        return apiBaseUri;
    }

    public String getGitBaseUri() {
        if (StringUtils.isBlank(gitBaseUri)) {
            gitBaseUri = TuleapConfiguration.get().getGitBaseUrl();
        }
        return gitBaseUri;
    }

    @Symbol("orangeforge")
    @Extension
    public static class DescriptorImpl extends SCMSourceDescriptor {

        @Override
        public String getDisplayName() {
            return "OrangeForge";
        }

        public List<SCMSourceTrait> getTraitsDefaults() {
            return Arrays.asList(new BranchDiscoveryTrait());
        }

        @RequirePOST
        @Restricted(NoExternalUse.class) // stapler
        public FormValidation doCheckCredentialsId(@AncestorInPath Item item, @QueryParameter String value,
            @CheckForNull @AncestorInPath Item context, @QueryParameter String apiUri,
            @QueryParameter String credentialsId) {
            return checkCredentials(item, apiUri);
        }

        public ListBoxModel doFillCredentialsIdItems(@CheckForNull @AncestorInPath Item context,
            @QueryParameter String apiUri, @QueryParameter String credentialsId) {
            return listScanCredentials(context, apiUri, credentialsId);
        }

        @Restricted(NoExternalUse.class) // stapler
        @SuppressWarnings("unused") // stapler
        public ListBoxModel doFillProjectIdItems(@CheckForNull @AncestorInPath Item context,
            @QueryParameter String projectId, @QueryParameter String credentialsId) throws IOException {
            String apiUri = TuleapConfiguration.get().getApiBaseUrl();
            final StandardCredentials credentials = lookupScanCredentials(context, apiUri, credentialsId);
            ListBoxModel result = new ListBoxModel();
            if (credentials != null && credentials instanceof StandardUsernamePasswordCredentials) {
                final TuleapClientRawCmd.AllUserProjects allUserProjectsRawCmd = new TuleapClientRawCmd().new AllUserProjects();
                final TuleapClientRawCmd.Command<List<TuleapProject>> configuredCmd = TuleapClientCommandConfigurer
                    .<List<TuleapProject>> newInstance(apiUri)
					.withCredentials(credentials).withCommand(allUserProjectsRawCmd)
                    .configure();

                configuredCmd.call().forEach(project -> {
                    ListBoxModel.Option newItem = new ListBoxModel.Option(project.getShortname(),
                        String.valueOf(project.getId()), project.getId() == Integer.valueOf(projectId));
                    result.add(newItem);
                });
            }
            return result;
        }

        @Restricted(NoExternalUse.class) // stapler
        public ListBoxModel doFillRepositoryPathItems(@CheckForNull @AncestorInPath Item context,
            @QueryParameter String projectId, @QueryParameter String credentialsId,
            @QueryParameter String repositoryPath) throws IOException {
            ListBoxModel result = new ListBoxModel();
            final String apiBaseUrl = TuleapConfiguration.get().getApiBaseUrl();
            StandardCredentials credentials = lookupScanCredentials(context, apiBaseUrl, credentialsId);
            final TuleapClientRawCmd.Command<List<TuleapGitRepository>> allRepositoriesByProjectRawCmd = new TuleapClientRawCmd().new AllRepositoriesByProject(
                projectId);
            TuleapClientCommandConfigurer.<List<TuleapGitRepository>> newInstance(apiBaseUrl)
                .withCredentials(credentials).withCommand(allRepositoriesByProjectRawCmd)
                .configure()
                .call()
                .stream().distinct().forEach(repo -> {
                    final ListBoxModel.Option newItem = new ListBoxModel.Option(repo.getName(), repo.getPath(),
                        repo.getPath().equals(repositoryPath));
                    result.add(newItem);
                });
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
