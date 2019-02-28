package org.jenkinsci.plugins.tuleap_git_branch_source;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import jenkins.plugins.git.GitSCMBuilder;
import jenkins.scm.api.*;
import jenkins.scm.api.trait.*;
import jenkins.scm.impl.UncategorizedSCMSourceCategory;
import jenkins.scm.impl.form.NamedArrayList;
import jenkins.scm.impl.trait.Discovery;
import jenkins.scm.impl.trait.Selection;
import jenkins.scm.impl.trait.WildcardSCMHeadFilterTrait;
import jenkins.scm.impl.trait.WildcardSCMSourceFilterTrait;
import net.jcip.annotations.GuardedBy;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.tuleap_git_branch_source.client.TuleapClientCommandConfigurer;
import org.jenkinsci.plugins.tuleap_git_branch_source.client.TuleapClientRawCmd;
import org.jenkinsci.plugins.tuleap_git_branch_source.client.api.TuleapGitRepository;
import org.jenkinsci.plugins.tuleap_git_branch_source.client.api.TuleapProject;
import org.jenkinsci.plugins.tuleap_git_branch_source.config.TuleapConfiguration;
import org.jenkinsci.plugins.tuleap_git_branch_source.config.TuleapConnector;
import org.jenkinsci.plugins.tuleap_git_branch_source.trait.UserForkRepositoryTrait;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.jenkinsci.plugins.tuleap_git_branch_source.config.TuleapConnector.*;

public class TuleapSCMNavigator extends SCMNavigator {

    private String projectId;
    private List<SCMTrait<? extends SCMTrait>> traits;
    private String credentialsId;
    private String apiUri, gitBaseUri;
    private Map<String, TuleapGitRepository> repositories = new HashMap<>();
    private TuleapProject project;

    @DataBoundConstructor
    public TuleapSCMNavigator(String projectId) {
        this.projectId = projectId;
    }

    @NonNull
    @Override
    protected String id() {
        return TuleapConfiguration.get().getDomainUrl() + "::" + projectId;
    }

    @Override
    public void visitSources(SCMSourceObserver observer) throws IOException, InterruptedException {
        TaskListener listener = observer.getListener();

        listener.getLogger().printf("Visit Sources of %s...%n", getprojectId());
        StandardCredentials credentials = TuleapConnector.lookupScanCredentials((Item) observer.getContext(),
            getApiUri(), credentialsId);

        try (final TuleapSCMNavigatorRequest request = new TuleapSCMNavigatorContext()
                .withTraits(traits).newRequest(this, observer)) {
            WitnessImpl witness = new WitnessImpl(listener, this);
            Optional<TuleapProject> project = TuleapClientCommandConfigurer.<Optional<TuleapProject>>newInstance(getApiUri())
                .withCredentials(credentials)
                .withCommand(new TuleapClientRawCmd.ProjectById(projectId))
                .configure()
                .call();
            if (project.isPresent()) {
                this.project = project.get();
            } else {
                //Should never happen though
                listener.getLogger().format("No project match projectId "+ projectId +"... it's weird%n");
                return;
            }
            Stream<TuleapGitRepository> repos = TuleapClientCommandConfigurer.<Stream<TuleapGitRepository>> newInstance
                (getApiUri())
				.withCredentials(credentials)
                .withCommand(new TuleapClientRawCmd.AllRepositoriesByProject(projectId))
                .configure()
                .call();

            for (TuleapGitRepository repo : repos.collect(Collectors.toList())) {
                repositories.put(repo.getName(), repo);
                SourceFactory sourceFactory = new SourceFactory(request, this.project, repo);
                if (request.process(repo.getName(), sourceFactory, null, witness)) {
                    listener.getLogger().format("%d repositories were processed (query completed)%n",
                        witness.getCount());
                }
            }
            listener.getLogger().format("%d repositories were processed%n", witness.getCount());
        }
    }

    @NonNull
    @Override
    protected List<Action> retrieveActions(@NonNull SCMNavigatorOwner owner, @CheckForNull SCMNavigatorEvent event,
        @NonNull TaskListener listener) throws IOException, InterruptedException {
        listener.getLogger().printf("Looking up details of %s...%n", getprojectId());
        List<Action> actions = new ArrayList<>();

        final StandardCredentials credentials = lookupScanCredentials((Item) owner, getApiUri(), credentialsId);
        Optional<TuleapProject> project = TuleapClientCommandConfigurer
            .<Optional<TuleapProject>> newInstance(getApiUri())
			.withCredentials(credentials)
            .withCommand(new TuleapClientRawCmd.ProjectById(projectId))
            .configure()
            .call();
        if (project.isPresent()) {
            actions.add(new TuleapProjectMetadataAction(project.get()));
            actions.add(new TuleapLink("icon-tuleap-logo", TuleapConfiguration.get().getDomainUrl() + "/projects/" +
                project.get().getShortname()));
        }
        return actions;
    }

    public List<SCMTrait<? extends SCMTrait>> getTraits() {
        return Collections.unmodifiableList(traits);
    }

    /**
     * Sets the behavioural traits that are applied to this navigator and any {@link TuleapSCMSource} instances it
     * discovers. The new traits will take affect on the next navigation through any of the
     * {@link #visitSources(SCMSourceObserver)} overloads or {@link #visitSource(String, SCMSourceObserver)}.
     *
     * @param traits
     *            the new behavioural traits.
     */
    @DataBoundSetter
    public void setTraits(@CheckForNull List<SCMTrait<? extends SCMTrait<?>>> traits) {
        this.traits = traits != null ? new ArrayList<>(traits) : new ArrayList<>();
    }

    /**
     * Gets the {@link StandardCredentials#getId()} of the credentials to use when accessing Tuleap (and also the
     * default credentials to use for checking out).
     *
     * @return the {@link StandardCredentials#getId()} of the credentials to use when accessing Tuleap (and also
     *         the default credentials to use for checking out).
     * @since 2.2.0
     */
    @CheckForNull
    public String getCredentialsId() {
        return credentialsId;
    }

    /**
     * Sets the {@link StandardCredentials#getId()} of the credentials to use when accessing Tuleap (and also the
     * default credentials to use for checking out).
     *
     * @param credentialsId
     *            the {@link StandardCredentials#getId()} of the credentials to use when accessing Tuleap (and also
     *            the default credentials to use for checking out).
     * @since 2.2.0
     */
    @DataBoundSetter
    public void setCredentialsId(@CheckForNull String credentialsId) {
        this.credentialsId = Util.fixEmpty(credentialsId);
    }

    /**
     * Gets the API endpoint for the Tuleap server.
     *
     * @return the API endpoint for the Tuleap server.
     */
    @CheckForNull
    public String getApiUri() {
        if (isBlank(apiUri)) {
            apiUri = TuleapConfiguration.get().getApiBaseUrl();
        }
        return apiUri;
    }

    /**
     * Gets the Git endpoint for Tuleap server.
     *
     * @return the Git endpoint for Tuleap server.
     */
    @CheckForNull
    public String getGitBaseUri() {
        if (isBlank(gitBaseUri)) {
            gitBaseUri = TuleapConfiguration.get().getGitBaseUrl();
        }
        return gitBaseUri;
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

    public Map<String, TuleapGitRepository> getRepositories() {
        return repositories;
    }

    public boolean isIncludeExcludesDefault() {
        String includes = "*";
        String excludes = "*";
        for (SCMTrait<?> trait : traits) {
            if (trait instanceof WildcardSCMHeadFilterTrait) {
                includes = ((WildcardSCMHeadFilterTrait) trait).getIncludes();
                excludes = ((WildcardSCMHeadFilterTrait) trait).getExcludes();
            }
        }

        return "*".equals(includes) && "*".equals(excludes);
    }

    @Symbol("tuleap")
    @Extension
    public static class DescriptorImpl extends SCMNavigatorDescriptor {

        private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());

        static {
            IconSet.icons.addIcon(new Icon("icon-tuleap-scm-navigator icon-sm",
                "plugin/tuleap-git-branch-source/images/16x16/tuleap-scmnavigator.png", Icon.ICON_SMALL_STYLE));
            IconSet.icons.addIcon(new Icon("icon-tuleap-scm-navigator icon-md",
                "plugin/tuleap-git-branch-source/images/24x24/tuleap-scmnavigator.png", Icon.ICON_MEDIUM_STYLE));
            IconSet.icons.addIcon(new Icon("icon-tuleap-scm-navigator icon-lg",
                "plugin/tuleap-git-branch-source/images/32x32/tuleap-scmnavigator.png", Icon.ICON_LARGE_STYLE));
            IconSet.icons.addIcon(new Icon("icon-tuleap-scm-navigator icon-xlg",
                "plugin/tuleap-git-branch-source/images/48x48/tuleap-scmnavigator.png", Icon.ICON_XLARGE_STYLE));

            IconSet.icons.addIcon(new Icon("icon-tuleap-logo icon-sm",
                "plugin/tuleap-git-branch-source/images/16x16/tuleap-logo.png", Icon.ICON_SMALL_STYLE));
            IconSet.icons.addIcon(new Icon("icon-tuleap-logo icon-md",
                "plugin/tuleap-git-branch-source/images/24x24/tuleap-logo.png", Icon.ICON_MEDIUM_STYLE));
            IconSet.icons.addIcon(new Icon("icon-tuleap-logo icon-lg",
                "plugin/tuleap-git-branch-source/images/32x32/tuleap-logo.png", Icon.ICON_LARGE_STYLE));
            IconSet.icons.addIcon(new Icon("icon-tuleap-logo icon-xlg",
                "plugin/tuleap-git-branch-source/images/48x48/tuleap-logo.png", Icon.ICON_XLARGE_STYLE));

            IconSet.icons.addIcon(new Icon("icon-git-repo icon-sm",
                "plugin/tuleap-git-branch-source/images/16x16/git-repo.png", Icon.ICON_SMALL_STYLE));
            IconSet.icons.addIcon(new Icon("icon-git-repo icon-md",
                "plugin/tuleap-git-branch-source/images/24x24/git-repo.png", Icon.ICON_MEDIUM_STYLE));
            IconSet.icons.addIcon(new Icon("icon-git-repo icon-lg",
                "plugin/tuleap-git-branch-source/images/32x32/git-repo.png", Icon.ICON_LARGE_STYLE));
            IconSet.icons.addIcon(new Icon("icon-git-repo icon-xlg",
                "plugin/tuleap-git-branch-source/images/48x48/git-repo.png", Icon.ICON_XLARGE_STYLE));

            IconSet.icons.addIcon(new Icon("icon-git-branch icon-sm",
                "plugin/tuleap-git-branch-source/images/16x16/git-branch.png", Icon.ICON_SMALL_STYLE));
            IconSet.icons.addIcon(new Icon("icon-git-branch icon-md",
                "plugin/tuleap-git-branch-source/images/24x24/git-branch.png", Icon.ICON_MEDIUM_STYLE));
            IconSet.icons.addIcon(new Icon("icon-git-branch icon-lg",
                "plugin/tuleap-git-branch-source/images/32x32/git-branch.png", Icon.ICON_LARGE_STYLE));
            IconSet.icons.addIcon(new Icon("icon-git-branch icon-xlg",
                "plugin/tuleap-git-branch-source/images/48x48/git-branch.png", Icon.ICON_XLARGE_STYLE));
        }

        @Inject
        private TuleapSCMSource.DescriptorImpl delegate;

        /**
         * {@inheritDoc}
         */
        @Override
        public String getPronoun() {
            return Messages.SCMNavigator_pronoun();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SCMNavigator newInstance(@CheckForNull String projectId) {
            TuleapSCMNavigator navigator = new TuleapSCMNavigator(projectId);
            List<SCMTrait<? extends SCMTrait<?>>> someTraits = getTraitsDefaults();
            someTraits.add(new UserForkRepositoryTrait(1));
            someTraits.add(new WildcardSCMSourceFilterTrait("", "*"));
            navigator.setTraits(someTraits);
            return navigator;
        }

        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public String getDisplayName() {
            return "Tuleap Project";
        }

        @NonNull
        protected SCMSourceCategory[] createCategories() {
            return new SCMSourceCategory[] {
                new UncategorizedSCMSourceCategory(Messages._SCMNavigator_depotSourceCategory()) };
        }

        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public String getDescription() {
            return Messages.SCMNavigator_description();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getIconFilePathPattern() {
            return "plugin/tuleap-git-branch-source/images/:size/tuleap-scmnavigator.png";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getIconClassName() {
            return "icon-tuleap-scm-navigator";
        }

        @RequirePOST
        @Restricted(NoExternalUse.class) // stapler
        public FormValidation doCheckCredentialsId(@CheckForNull @AncestorInPath Item context,
            @QueryParameter String apiUri, @QueryParameter String credentialsId) {

            return checkCredentials(context, apiUri ,credentialsId);
        }

        @Restricted(NoExternalUse.class) // stapler
        public FormValidation doCheckProjectId(@Nonnull @AncestorInPath Item context,
            @QueryParameter String projectId, @QueryParameter String includes, @QueryParameter String excludes) {


            Optional<SCMNavigator> navigator = ((OrganizationFolder) context).getNavigators().stream().filter(n
                                                                                                                       -> n
                instanceof TuleapSCMNavigator).findFirst();
            if (navigator.isPresent()) {
                if (((TuleapSCMNavigator)navigator.get()).isIncludeExcludesDefault()) {
                    return FormValidation.warning("Without modifications of include/exlude parameters everything will be ignored");
                } else {
                    return FormValidation.ok();
                }
            } else {
                return FormValidation.error("Error fetching navigator ... weird");
            }
        }

        /**
         * Populates the drop-down list of credentials.
         *
         * @param context
         *            the context.
         * @return the drop-down list.
         * @since 2.2.0
         */
        @Restricted(NoExternalUse.class) // stapler
        public ListBoxModel doFillCredentialsIdItems(@CheckForNull @AncestorInPath Item context,
            @QueryParameter String apiUri, @QueryParameter String credentialsId) {
            return listScanCredentials(context, apiUri, credentialsId, true);
        }

        @SuppressWarnings("unused") // jelly
        public List<SCMTrait<? extends SCMTrait<?>>> getTraitsDefaults() {
            List<SCMTrait<? extends SCMTrait<?>>> result = new ArrayList<>();
            result.addAll(delegate.getTraitsDefaults());
            return result;
        }

        /**
         * Returns the available GitHub endpoint items.
         *
         * @return the available GitHub endpoint items.
         */
        @Restricted(NoExternalUse.class) // stapler
        @SuppressWarnings("unused") // stapler
        public ListBoxModel doFillApiUriItems() {
            ListBoxModel result = new ListBoxModel();
            result.add("Tuleap API", TuleapConfiguration.get().getApiBaseUrl());
            return result;
        }

        @Restricted(NoExternalUse.class) // stapler
        @SuppressWarnings("unused") // stapler
        public ListBoxModel doFillProjectIdItems(@CheckForNull @AncestorInPath Item context,
            @QueryParameter String credentialsId) throws IOException {
            String apiUri = TuleapConfiguration.get().getApiBaseUrl();
            final StandardCredentials credentials = lookupScanCredentials(context, apiUri, credentialsId);
            ListBoxModel result = new ListBoxModel();
            try {
                TuleapClientCommandConfigurer.<Stream<TuleapProject>>newInstance(apiUri)
                    .withCredentials(credentials)
                    .withCommand(new TuleapClientRawCmd.AllUserProjects(true))
                    .configure()
                    .call()
                    .forEach(project -> result.add(project.getShortname(), String.valueOf(project.getId())));
            } catch (IOException | IllegalArgumentException e) {
                result.clear();
            }
            return result;
        }

        /**
         * Returns the available GitHub endpoint items.
         *
         * @return the available GitHub endpoint items.
         */
        @Restricted(NoExternalUse.class) // stapler
        @SuppressWarnings("unused") // stapler
        public ListBoxModel doFillGitBaseUriItems() {
            ListBoxModel result = new ListBoxModel();
            result.add("Tuleap Git", TuleapConfiguration.get().getGitBaseUrl());
            return result;
        }

        /**
         * Returns the {@link SCMTraitDescriptor} instances grouped into categories.
         *
         * @return the categorized list of {@link SCMTraitDescriptor} instances.
         * @since 2.2.0
         */
        @SuppressWarnings("unused") // jelly
        public List<NamedArrayList<? extends SCMTraitDescriptor<?>>> getTraitsDescriptorLists() {
            TuleapSCMSource.DescriptorImpl sourceDescriptor = Jenkins.getActiveInstance()
                                                                     .getDescriptorByType(TuleapSCMSource.DescriptorImpl.class);
            List<SCMTraitDescriptor<?>> all = new ArrayList<>();
            all.addAll(SCMNavigatorTrait._for(this, TuleapSCMNavigatorContext.class, TuleapSCMSourceBuilder.class));
            all.addAll(SCMSourceTrait._for(sourceDescriptor, TuleapSCMSourceContext.class, null));
            all.addAll(SCMSourceTrait._for(sourceDescriptor, null, GitSCMBuilder.class));
            List<SCMTraitDescriptor<?>> distinct = all.stream().distinct().collect(Collectors.toList());

            List<NamedArrayList<? extends SCMTraitDescriptor<?>>> result = new ArrayList<>();
            NamedArrayList.select(distinct, "Repositories",
                scmTraitDescriptor -> scmTraitDescriptor instanceof SCMNavigatorTraitDescriptor, true, result);
            NamedArrayList.select(distinct, "Within repository", NamedArrayList
                .anyOf(NamedArrayList.withAnnotation(Discovery.class), NamedArrayList.withAnnotation(Selection.class)),
                true, result);
            NamedArrayList.select(distinct, "Additional", null, true, result);
            return result;
        }
    }

    /**
     * A {@link SCMNavigatorRequest.Witness} that counts how many sources have been observed.
     */
    private static class WitnessImpl implements SCMNavigatorRequest.Witness {
        /**
         * The listener to log to.
         */
        private final TaskListener listener;
        /**
         * The count of repositories matches.
         */
        @GuardedBy("this")
        private int count;

        private TuleapSCMNavigator navigator;

        private WitnessImpl(TaskListener listener, TuleapSCMNavigator navigator) {
            this.listener = listener;
            this.navigator = navigator;
        }

        @Override
        public void record(@NonNull String name, boolean isMatch) {
            if (isMatch) {
                listener.getLogger().format("Proposing %s%n", name);
                synchronized (this) {
                    count++;
                }
            } else {
                TuleapGitRepository repo = navigator.getRepositories().get(name);
                listener.getLogger().format("Ignoring %s (reason : private user fork is excluded, URL : %s)%n",
                                            name, repo.getPath());
            }
        }

        /**
         * Returns the count of repositories matches.
         *
         * @return the count of repositories matches.
         */
        public synchronized int getCount() {
            return count;
        }
    }

    private class SourceFactory implements SCMNavigatorRequest.SourceLambda {

        private final TuleapSCMNavigatorRequest request;
        private final TuleapGitRepository repo;
        private final TuleapProject project;

        public SourceFactory(TuleapSCMNavigatorRequest request, TuleapProject project, TuleapGitRepository repo) {
            this.request = request;
            this.repo = repo;
            this.project = project;
        }

        @NonNull
        @Override
        public SCMSource create(@NonNull String repositoryName) throws IOException, InterruptedException {
            return new TuleapSCMSourceBuilder(getId() + repositoryName, credentialsId, project, repo)
                .withRequest(request).build();
        }
    }
}
