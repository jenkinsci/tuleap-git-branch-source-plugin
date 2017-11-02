package com.francetelecom.faas.jenkinsfaasbranchsource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import javax.inject.Inject;


import org.apache.commons.lang.StringUtils;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.TuleapClientCommandConfigurer;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.TuleapClientRawCmd;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapGitRepository;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapProject;
import com.francetelecom.faas.jenkinsfaasbranchsource.config.TuleapConfiguration;
import com.francetelecom.faas.jenkinsfaasbranchsource.config.TuleapConnector;
import com.francetelecom.faas.jenkinsfaasbranchsource.trait.UserForkRepositoryTrait;

import static com.francetelecom.faas.jenkinsfaasbranchsource.config.TuleapConnector.checkCredentials;
import static com.francetelecom.faas.jenkinsfaasbranchsource.config.TuleapConnector.listScanCredentials;
import static com.francetelecom.faas.jenkinsfaasbranchsource.config.TuleapConnector.lookupScanCredentials;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.plugins.git.GitSCMBuilder;
import jenkins.plugins.git.traits.GitBrowserSCMSourceTrait;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMNavigatorDescriptor;
import jenkins.scm.api.SCMNavigatorEvent;
import jenkins.scm.api.SCMNavigatorOwner;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCategory;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.trait.SCMNavigatorRequest;
import jenkins.scm.api.trait.SCMNavigatorTrait;
import jenkins.scm.api.trait.SCMNavigatorTraitDescriptor;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMTrait;
import jenkins.scm.api.trait.SCMTraitDescriptor;
import jenkins.scm.impl.UncategorizedSCMSourceCategory;
import jenkins.scm.impl.form.NamedArrayList;
import jenkins.scm.impl.trait.Discovery;
import jenkins.scm.impl.trait.Selection;
import jenkins.scm.impl.trait.WildcardSCMSourceFilterTrait;
import net.jcip.annotations.GuardedBy;

public class TuleapSCMNavigator extends SCMNavigator {

    private String projectId;
    private List<SCMTrait<? extends SCMTrait>> traits;
    private String credentialsId;
    private String apiUri, gitBaseUri;
    private Map<String, TuleapGitRepository> repositories = new HashMap<>();

    @DataBoundConstructor
    public TuleapSCMNavigator() {
    }

    @NonNull
    @Override
    protected String id() {
        return "https://www.forge.orange-labs.fr/projects::" + projectId;
    }

    @Override
    public void visitSources(SCMSourceObserver observer) throws IOException, InterruptedException {
        TaskListener listener = observer.getListener();

        if (StringUtils.isBlank(getprojectId())) {
            listener.getLogger().format("Must specify a project Id%n");
            return;
        }
        listener.getLogger().printf("Visit Sources of %s...%n", getprojectId());
        StandardCredentials credentials = TuleapConnector.lookupScanCredentials((Item) observer.getContext(),
            getApiUri(), credentialsId);

        try (final TuleapSCMNavigatorRequest request = new TuleapSCMNavigatorContext()
                .withTraits(traits).newRequest(this, observer)) {
            WitnessImpl witness = new WitnessImpl(listener);
            final TuleapClientRawCmd.Command<List<TuleapGitRepository>> allRepositoriesByProjectRawCmd = new
					TuleapClientRawCmd().new AllRepositoriesByProject(
                projectId);
            final TuleapClientRawCmd.Command<List<TuleapGitRepository>> configuredCmd = TuleapClientCommandConfigurer
                .<List<TuleapGitRepository>> newInstance(getApiUri())
				.withCredentials(credentials).withCommand(allRepositoriesByProjectRawCmd)
                .configure();

            for (TuleapGitRepository repo : configuredCmd.call()) {
                repositories.put(repo.getName(), repo);
                SourceFactory sourceFactory = new SourceFactory(request, repo);
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
        final TuleapClientRawCmd.Command<Optional<TuleapProject>> projectByIdRawCmd = new TuleapClientRawCmd().new
            ProjectById(projectId);

        final TuleapClientRawCmd.Command<Optional<TuleapProject>> configuredCmd = TuleapClientCommandConfigurer
            .<Optional<TuleapProject>> newInstance(getApiUri())
			.withCredentials(credentials).withCommand(projectByIdRawCmd)
            .configure();
        final Optional<TuleapProject> project = configuredCmd.call();
        if (project.isPresent()) {
            actions.add(new TuleapProjectMetadataAction(project.get()));
            actions.add(new TuleapLink("icon-orangeforge-logo",TuleapConfiguration.ORANGEFORGE_URL + "/projects/" +
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
     * Gets the {@link StandardCredentials#getId()} of the credentials to use when accessing OrangeForge (and also the
     * default credentials to use for checking out).
     *
     * @return the {@link StandardCredentials#getId()} of the credentials to use when accessing OrangeForge (and also
     *         the default credentials to use for checking out).
     * @since 2.2.0
     */
    @CheckForNull
    public String getCredentialsId() {
        return credentialsId;
    }

    /**
     * Sets the {@link StandardCredentials#getId()} of the credentials to use when accessing OrangeForge (and also the
     * default credentials to use for checking out).
     *
     * @param credentialsId
     *            the {@link StandardCredentials#getId()} of the credentials to use when accessing OrangeForge (and also
     *            the default credentials to use for checking out).
     * @since 2.2.0
     */
    @DataBoundSetter
    public void setCredentialsId(@CheckForNull String credentialsId) {
        this.credentialsId = Util.fixEmpty(credentialsId);
    }

    /**
     * Gets the API endpoint for the OrangeForge server.
     *
     * @return the API endpoint for the OrangeForge server.
     */
    @CheckForNull
    public String getApiUri() {
        if (StringUtils.isBlank(apiUri)) {
            apiUri = TuleapConfiguration.get().getApiBaseUrl();
        }
        return apiUri;
    }

    /**
     * Gets the Git endpoint for OrangeForge server.
     *
     * @return the Git endpoint for OrangeForge server.
     */
    @CheckForNull
    public String getGitBaseUri() {
        if (StringUtils.isBlank(gitBaseUri)) {
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

    @Symbol("orangeforge")
    @Extension
    public static class DescriptorImpl extends SCMNavigatorDescriptor {

        private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());

        static {
            IconSet.icons.addIcon(new Icon("icon-orangeforge-scm-navigator icon-sm",
                "plugin/jenkins-faas-branch-source/images/16x16/orangeforge-scmnavigator.png", Icon.ICON_SMALL_STYLE));
            IconSet.icons.addIcon(new Icon("icon-orangeforge-scm-navigator icon-md",
                "plugin/jenkins-faas-branch-source/images/24x24/orangeforge-scmnavigator.png", Icon.ICON_MEDIUM_STYLE));
            IconSet.icons.addIcon(new Icon("icon-orangeforge-scm-navigator icon-lg",
                "plugin/jenkins-faas-branch-source/images/32x32/orangeforge-scmnavigator.png", Icon.ICON_LARGE_STYLE));
            IconSet.icons.addIcon(new Icon("icon-orangeforge-scm-navigator icon-xlg",
                "plugin/jenkins-faas-branch-source/images/48x48/orangeforge-scmnavigator.png", Icon.ICON_XLARGE_STYLE));

            IconSet.icons.addIcon(new Icon("icon-orangeforge-logo icon-sm",
                "plugin/jenkins-faas-branch-source/images/16x16/orangeforge-logo.png", Icon.ICON_SMALL_STYLE));
            IconSet.icons.addIcon(new Icon("icon-orangeforge-logo icon-md",
                "plugin/jenkins-faas-branch-source/images/24x24/orangeforge-logo.png", Icon.ICON_MEDIUM_STYLE));
            IconSet.icons.addIcon(new Icon("icon-orangeforge-logo icon-lg",
                "plugin/jenkins-faas-branch-source/images/32x32/orangeforge-logo.png", Icon.ICON_LARGE_STYLE));
            IconSet.icons.addIcon(new Icon("icon-orangeforge-logo icon-xlg",
                "plugin/jenkins-faas-branch-source/images/48x48/orangeforge-logo.png", Icon.ICON_XLARGE_STYLE));

            IconSet.icons.addIcon(new Icon("icon-git-repo icon-sm",
                "plugin/jenkins-faas-branch-source/images/16x16/git-repo.png", Icon.ICON_SMALL_STYLE));
            IconSet.icons.addIcon(new Icon("icon-git-repo icon-md",
                "plugin/jenkins-faas-branch-source/images/24x24/git-repo.png", Icon.ICON_MEDIUM_STYLE));
            IconSet.icons.addIcon(new Icon("icon-git-repo icon-lg",
                "plugin/jenkins-faas-branch-source/images/32x32/git-repo.png", Icon.ICON_LARGE_STYLE));
            IconSet.icons.addIcon(new Icon("icon-git-repo icon-xlg",
                "plugin/jenkins-faas-branch-source/images/48x48/git-repo.png", Icon.ICON_XLARGE_STYLE));

            IconSet.icons.addIcon(new Icon("icon-git-branch icon-sm",
                "plugin/jenkins-faas-branch-source/images/16x16/git-branch.png", Icon.ICON_SMALL_STYLE));
            IconSet.icons.addIcon(new Icon("icon-git-branch icon-md",
                "plugin/jenkins-faas-branch-source/images/24x24/git-branch.png", Icon.ICON_MEDIUM_STYLE));
            IconSet.icons.addIcon(new Icon("icon-git-branch icon-lg",
                "plugin/jenkins-faas-branch-source/images/32x32/git-branch.png", Icon.ICON_LARGE_STYLE));
            IconSet.icons.addIcon(new Icon("icon-git-branch icon-xlg",
                "plugin/jenkins-faas-branch-source/images/48x48/git-branch.png", Icon.ICON_XLARGE_STYLE));
        }

        @Inject
        private TuleapSCMSource.DescriptorImpl delegate;

        /**
         * {@inheritDoc}
         */
        @Override
        public String getPronoun() {
            return Messages.OFSCMNavigator_pronoun();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SCMNavigator newInstance(@CheckForNull String projectId) {
            TuleapSCMNavigator navigator = new TuleapSCMNavigator();
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
            return "OrangeForge Project";
        }

        @NonNull
        protected SCMSourceCategory[] createCategories() {
            return new SCMSourceCategory[] {
                new UncategorizedSCMSourceCategory(Messages._OFSCMNavigator_depotSourceCategory()) };
        }

        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public String getDescription() {
            return Messages.OFSCMNavigator_description();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getIconFilePathPattern() {
            return "plugin/jenkins-faas-branch-source/images/:size/orangeforge-scmnavigator.png";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getIconClassName() {
            return "icon-orangeforge-scm-navigator";
        }

        @RequirePOST
        @Restricted(NoExternalUse.class) // stapler
        public FormValidation doCheckCredentialsId(@CheckForNull @AncestorInPath Item context,
            @QueryParameter String apiUri, @QueryParameter String credentialsId) {

            return checkCredentials(context, apiUri ,credentialsId);
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
            return listScanCredentials(context, apiUri, credentialsId);
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
            result.add("OrangeForge API", TuleapConfiguration.get().getApiBaseUrl());
            return result;
        }

        @Restricted(NoExternalUse.class) // stapler
        @SuppressWarnings("unused") // stapler
        public ListBoxModel doFillProjectIdItems(@CheckForNull @AncestorInPath Item context,
            @QueryParameter String credentialsId) throws IOException {
            String apiUri = TuleapConfiguration.get().getApiBaseUrl();
            final StandardCredentials credentials = lookupScanCredentials(context, apiUri, credentialsId);
            ListBoxModel result = new ListBoxModel();
            if (credentials != null && credentials instanceof StandardUsernamePasswordCredentials) {
                final TuleapClientRawCmd.AllUserProjects allUserProjectsRawCmd = new TuleapClientRawCmd().new
                    AllUserProjects(true);
                final TuleapClientRawCmd.Command<List<TuleapProject>> configuredCmd = TuleapClientCommandConfigurer
                    .<List<TuleapProject>> newInstance(apiUri)
					.withCredentials(credentials).withCommand(allUserProjectsRawCmd)
                    .configure();

                configuredCmd.call()
                    .forEach(project -> result.add(project.getShortname(), String.valueOf(project.getId())));
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
            result.add("OrangeForge Git", TuleapConfiguration.get().getGitBaseUrl());
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
            Set<SCMTraitDescriptor<?>> dedup = new HashSet<>();
            for (Iterator<SCMTraitDescriptor<?>> iterator = all.iterator(); iterator.hasNext();) {
                SCMTraitDescriptor<?> d = iterator.next();
                if (dedup.contains(d) || d instanceof GitBrowserSCMSourceTrait.DescriptorImpl) {
                    // FIXME
                    // remove any we have seen already and ban the browser configuration as it will always be github
                    iterator.remove();
                } else {
                    dedup.add(d);
                }
            }
            List<NamedArrayList<? extends SCMTraitDescriptor<?>>> result = new ArrayList<>();
            NamedArrayList.select(all, "Repositories",
                scmTraitDescriptor -> scmTraitDescriptor instanceof SCMNavigatorTraitDescriptor, true, result);
            NamedArrayList.select(all, "Within repository", NamedArrayList
                .anyOf(NamedArrayList.withAnnotation(Discovery.class), NamedArrayList.withAnnotation(Selection.class)),
                true, result);
            NamedArrayList.select(all, "Additional", null, true, result);
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

        private WitnessImpl(TaskListener listener) {
            this.listener = listener;
        }

        @Override
        public void record(@NonNull String name, boolean isMatch) {
            if (isMatch) {
                listener.getLogger().format("Proposing %s%n", name);
                synchronized (this) {
                    count++;
                }
            } else {
                listener.getLogger().format("Ignoring %s%n", name);
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

        public SourceFactory(TuleapSCMNavigatorRequest request, TuleapGitRepository repo) {
            this.request = request;
            this.repo = repo;
        }

        @NonNull
        @Override
        public SCMSource create(@NonNull String repositoryName) throws IOException, InterruptedException {
            return new TuleapSCMSourceBuilder(getId() + repositoryName, credentialsId, projectId, repo.getPath())
                .withRequest(request).build();
        }
    }
}
