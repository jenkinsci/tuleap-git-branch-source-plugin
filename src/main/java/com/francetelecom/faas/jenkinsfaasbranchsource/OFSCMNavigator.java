package com.francetelecom.faas.jenkinsfaasbranchsource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;


import org.apache.commons.lang.StringUtils;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.francetelecom.faas.jenkinsfaasbranchsource.config.OrangeForgeSettings;
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFGitRepository;
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFProject;
import com.francetelecom.faas.jenkinsfaasbranchsource.trait.OFUserForkRepositoryTrait;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.RestrictedSince;
import hudson.Util;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.TaskListener;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
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
import jenkins.scm.impl.trait.RegexSCMSourceFilterTrait;
import jenkins.scm.impl.trait.Selection;
import jenkins.scm.impl.trait.WildcardSCMSourceFilterTrait;
import net.jcip.annotations.GuardedBy;

public class OFSCMNavigator extends SCMNavigator {

	private String projectId;
	private OFProject project;
	private List<SCMTrait<? extends SCMTrait>> traits;
	private String credentialsId;

	@DataBoundConstructor
	public OFSCMNavigator() {
		this.traits = Arrays.asList(new WildcardSCMSourceFilterTrait("", "*"),
			new RegexSCMSourceFilterTrait("^/u/.*$"));
	}

	@Override
	protected String id() {
		return "https://www.forge.orange-labs.fr/projects::" + project;
	}

	@Override
	public void visitSources(SCMSourceObserver observer) throws IOException, InterruptedException {
		TaskListener listener = observer.getListener();

		if (StringUtils.isBlank(getprojectId())) {
			listener.getLogger().format("Must specify a project Id%n");
			return;
		}
		listener.getLogger().printf("Visit Sources of %s...%n", getprojectId());

		final OrangeForgeSettings orangeForgeSettings = new OrangeForgeSettings();
		StandardUsernamePasswordCredentials credentials = orangeForgeSettings.credentials();
		OFClient client = new OFClient(orangeForgeSettings);

		try (final OFSCMNavigatorRequest request = new OFSCMNavigatorContext()
				.withTraits(traits)
				.newRequest(this, observer)) {
			SourceFactory sourceFactory = new SourceFactory(request);
			WitnessImpl witness = new WitnessImpl(listener);
			for (OFGitRepository repo : client.projectRepositories()) {
				if (request.process(repo.getPath(), sourceFactory, null, witness)) {
					listener.getLogger().format(
							"%d repositories were processed (query completed)%n", witness.getCount());
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

		//TODO standardize & factorize somewhere else
		final OrangeForgeSettings orangeForgeSettings = new OrangeForgeSettings();
		StandardUsernamePasswordCredentials credentials = orangeForgeSettings.credentials();
		OFClient client = new OFClient(orangeForgeSettings);

		final OFProject project = client.projectById(getprojectId());
		actions.add(new OFProjectMetadataAction(project));
		return actions;
	}

	public List<SCMTrait<? extends SCMTrait>> getTraits() {
		return Collections.unmodifiableList(traits);
	}

	/**
	 * Sets the behavioural traits that are applied to this navigator and any {@link OFSCMSource} instances it
	 * discovers. The new traits will take affect on the next navigation through any of the
	 * {@link #visitSources(SCMSourceObserver)} overloads or {@link #visitSource(String, SCMSourceObserver)}.
	 *
	 * @param traits the new behavioural traits.
	 */
	@DataBoundSetter
	public void setTraits(@CheckForNull List<SCMTrait<? extends SCMTrait<?>>> traits) {
		this.traits = traits != null ? new ArrayList<>(traits) : new ArrayList<>();
	}

	/**
	 * Gets the {@link StandardCredentials#getId()} of the credentials to use when accessing OrangeForge (and also
	 * the default credentials to use for checking out).
	 *
	 * @return the {@link StandardCredentials#getId()} of the credentials to use when accessing OrangeForge (and
	 * also the default credentials to use for checking out).
	 * @since 2.2.0
	 */
	@CheckForNull
	public String getCredentialsId() {
		return credentialsId;
	}

	/**
	 * Sets the {@link StandardCredentials#getId()} of the credentials to use when accessing OrangeForge (and also
	 * the default credentials to use for checking out).
	 *
	 * @param credentialsId the {@link StandardCredentials#getId()} of the credentials to use when accessing
	 *                      OrangeForge (and also the default credentials to use for checking out).
	 * @since 2.2.0
	 */
	@DataBoundSetter
	public void setCredentialsId(@CheckForNull String credentialsId) {
		this.credentialsId = Util.fixEmpty(credentialsId);
	}

	/**
	 * Gets the Id of the project who's repositories will be navigated.
	 * @return the Idof the project who's repositories will be navigated.
	 */
	public String getprojectId() {
		return projectId;
	}

	@DataBoundSetter
	public void setProjectId(final String projectId) {
		this.projectId = projectId;
	}

	@Symbol("orangeforge")
	@Extension
	public static class DescriptorImpl extends SCMNavigatorDescriptor {

		private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());

		@Deprecated
		@Restricted(DoNotUse.class)
		@RestrictedSince("2.2.0")
		public static final String defaultIncludes = "";
		@Deprecated
		@Restricted(DoNotUse.class)
		@RestrictedSince("2.2.0")
		public static final String defaultExcludes = "*";

		@Inject private OFSCMSource.DescriptorImpl delegate;


		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPronoun() {
			return Messages.OFSCMNavigator_Pronoun();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public SCMNavigator newInstance(@CheckForNull String projectId) {
			OFSCMNavigator navigator = new OFSCMNavigator();
			List<SCMTrait<? extends SCMTrait<?>>> someTraits = getTraitsDefaults();
			someTraits.add(new OFUserForkRepositoryTrait(1));
			someTraits.add(new WildcardSCMSourceFilterTrait("", "*"));
			navigator.setTraits(someTraits);
			return navigator;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getDisplayName() {
			return "OrangeForge Project";
		}

		protected SCMSourceCategory[] createCategories() {
			return new SCMSourceCategory[]{
					new UncategorizedSCMSourceCategory(Messages._OFSCMNavigator_DepotSourceCategory())
			};
		}

		/**
		 * {@inheritDoc}
		 */
		@NonNull
		@Override
		public String getDescription() {
			return Messages.OFSCMNavigator_Description();
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
		public FormValidation doCheckCredentialsId( @AncestorInPath Item item, @QueryParameter String value,
													@CheckForNull @AncestorInPath Item context, @QueryParameter String credentialsId ) {
			if (item == null) {
				if (!Jenkins.getActiveInstance().hasPermission(Jenkins.ADMINISTER)) {
					return FormValidation.ok();
				}
			} else {
				if (!item.hasPermission(Item.EXTENDED_READ)
						&& !item.hasPermission(CredentialsProvider.USE_ITEM)) {
					return FormValidation.ok();
				}
			}
			// TODO check credential exists, ask orangeforge if credentials are valid credentials and then ok else
			// invalid
			/*if (CredentialsProvider.listCredentials(StandardUsernamePasswordCredentials.class,
			item,
			item instanceof Queue.Task
					? Tasks.getAuthenticationOf((Queue.Task)item) : ACL.SYSTEM, URIRequirementBuilder.fromUri(apiUri),
			null ).isEmpty()) {
				return FormValidation.error("Cannot find currently selected credentials");
			}*/
			return FormValidation.ok();
		}

		@SuppressWarnings("unused") // jelly
		public List<SCMTrait<? extends SCMTrait<?>>> getTraitsDefaults() {
			List<SCMTrait<? extends SCMTrait<?>>> result = new ArrayList<>();
			result.addAll(delegate.getTraitsDefaults());
			//result.add();
			return result;
		}

		static {
			IconSet.icons.addIcon(
					new Icon("icon-orangeforge-scm-navigator icon-sm",
							 "plugin/jenkins-faas-branch-source/images/16x16/orangeforge-scmnavigator.png",
							 Icon.ICON_SMALL_STYLE));
			IconSet.icons.addIcon(
					new Icon("icon-orangeforge-scm-navigator icon-md",
							 "plugin/jenkins-faas-branch-source/images/24x24/orangeforge-scmnavigator.png",
							 Icon.ICON_MEDIUM_STYLE));
			IconSet.icons.addIcon(
					new Icon("icon-orangeforge-scm-navigator icon-lg",
							 "plugin/jenkins-faas-branch-source/images/32x32/orangeforge-scmnavigator.png",
							 Icon.ICON_LARGE_STYLE));
			IconSet.icons.addIcon(
					new Icon("icon-orangeforge-scm-navigator icon-xlg",
							 "plugin/jenkins-faas-branch-source/images/48x48/orangeforge-scmnavigator.png",
							 Icon.ICON_XLARGE_STYLE));

			IconSet.icons.addIcon(
					new Icon("icon-github-logo icon-sm",
							 "plugin/jenkins-faas-branch-source/images/16x16/git-logo.png",
							 Icon.ICON_SMALL_STYLE));
			IconSet.icons.addIcon(
					new Icon("icon-github-logo icon-md",
							 "plugin/jenkins-faas-branch-source/images/24x24/git-logo.png",
							 Icon.ICON_MEDIUM_STYLE));
			IconSet.icons.addIcon(
					new Icon("icon-github-logo icon-lg",
							 "plugin/jenkins-faas-branch-source/images/32x32/git-logo.png",
							 Icon.ICON_LARGE_STYLE));
			IconSet.icons.addIcon(
					new Icon("icon-github-logo icon-xlg",
							 "plugin/jenkins-faas-branch-source/images/48x48/git-logo.png",
							 Icon.ICON_XLARGE_STYLE));

			IconSet.icons.addIcon(
					new Icon("icon-github-repo icon-sm",
							 "plugin/jenkins-faas-branch-source/images/16x16/git-repo.png",
							 Icon.ICON_SMALL_STYLE));
			IconSet.icons.addIcon(
					new Icon("icon-github-repo icon-md",
							 "plugin/jenkins-faas-branch-source/images/24x24/git-repo.png",
							 Icon.ICON_MEDIUM_STYLE));
			IconSet.icons.addIcon(
					new Icon("icon-github-repo icon-lg",
							 "plugin/jenkins-faas-branch-source/images/32x32/git-repo.png",
							 Icon.ICON_LARGE_STYLE));
			IconSet.icons.addIcon(
					new Icon("icon-github-repo icon-xlg",
							 "plugin/jenkins-faas-branch-source/images/48x48/git-repo.png",
							 Icon.ICON_XLARGE_STYLE));

			IconSet.icons.addIcon(
					new Icon("icon-github-branch icon-sm",
							 "plugin/jenkins-faas-branch-source/images/16x16/git-branch.png",
							 Icon.ICON_SMALL_STYLE));
			IconSet.icons.addIcon(
					new Icon("icon-github-branch icon-md",
							 "plugin/jenkins-faas-branch-source/images/24x24/git-branch.png",
							 Icon.ICON_MEDIUM_STYLE));
			IconSet.icons.addIcon(
					new Icon("icon-github-branch icon-lg",
							 "plugin/jenkins-faas-branch-source/images/32x32/git-branch.png",
							 Icon.ICON_LARGE_STYLE));
			IconSet.icons.addIcon(
					new Icon("icon-github-branch icon-xlg",
							 "plugin/jenkins-faas-branch-source/images/48x48/git-branch.png",
							 Icon.ICON_XLARGE_STYLE));
		}

		/**
		 * Populates the drop-down list of credentials.
		 *
		 * @param context the context.
		 * @return the drop-down list.
		 * @since 2.2.0
		 */
		@Restricted(NoExternalUse.class) // stapler
		public ListBoxModel doFillCredentialsIdItems(@CheckForNull @AncestorInPath Item context,
													 @QueryParameter String credentialsId) {
			if (context == null
					? !Jenkins.getActiveInstance().hasPermission(Jenkins.ADMINISTER)
					: !context.hasPermission(Item.EXTENDED_READ)) {
				return new StandardListBoxModel().includeCurrentValue(credentialsId);
			}
			return new StandardListBoxModel()
					.includeEmptyValue()
					.includeMatchingAs(
							context instanceof Queue.Task
									? Tasks.getDefaultAuthenticationOf((Queue.Task) context)
									: ACL.SYSTEM,
							context,
							StandardUsernameCredentials.class,
							URIRequirementBuilder.fromUri(StringUtils.defaultString("https://www.forge.orange-labs.fr/api")).build(),
							CredentialsMatchers.anyOf(CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class))
					);
		}

		/**
		 * Returns the {@link SCMTraitDescriptor} instances grouped into categories.
		 *
		 * @return the categorized list of {@link SCMTraitDescriptor} instances.
		 * @since 2.2.0
		 */
		@SuppressWarnings("unused") // jelly
		public List<NamedArrayList<? extends SCMTraitDescriptor<?>>> getTraitsDescriptorLists() {
			OFSCMSource.DescriptorImpl sourceDescriptor =
					Jenkins.getActiveInstance().getDescriptorByType(OFSCMSource.DescriptorImpl.class);
			List<SCMTraitDescriptor<?>> all = new ArrayList<>();
			all.addAll(SCMNavigatorTrait._for(this, OFSCMNavigatorContext.class, OFSCMSourceBuilder.class));
			all.addAll(SCMSourceTrait._for(sourceDescriptor, OFSCMSourceContext.class, null));
			all.addAll(SCMSourceTrait._for(sourceDescriptor, null, GitSCMBuilder.class));
			Set<SCMTraitDescriptor<?>> dedup = new HashSet<>();
			for (Iterator<SCMTraitDescriptor<?>> iterator = all.iterator(); iterator.hasNext(); ) {
				SCMTraitDescriptor<?> d = iterator.next();
				if (dedup.contains(d)
						|| d instanceof GitBrowserSCMSourceTrait.DescriptorImpl) {
					// remove any we have seen already and ban the browser configuration as it will always be github
					iterator.remove();
				} else {
					dedup.add(d);
				}
			}
			List<NamedArrayList<? extends SCMTraitDescriptor<?>>> result = new ArrayList<>();
			NamedArrayList.select(all, "Repositories", new NamedArrayList.Predicate<SCMTraitDescriptor<?>>() {
									  @Override
									  public boolean test(SCMTraitDescriptor<?> scmTraitDescriptor) {
										  return scmTraitDescriptor instanceof SCMNavigatorTraitDescriptor;
									  }
								  },
								  true, result);
			NamedArrayList.select(all, "Within repository", NamedArrayList.anyOf(NamedArrayList.withAnnotation(Discovery.class), NamedArrayList.withAnnotation(Selection.class)),
								  true, result);
			NamedArrayList.select(all, "Additional", null, true, result);
			return result;
		}
	}

	private class SourceFactory implements SCMNavigatorRequest.SourceLambda {

		private final OFSCMNavigatorRequest request;

		public SourceFactory(OFSCMNavigatorRequest request) {
			this.request = request;
		}

		@NonNull
		@Override
		public SCMSource create(@NonNull String repositoryName) throws IOException, InterruptedException {
			return new OFSCMSourceBuilder(getId()+repositoryName, credentialsId, projectId, repositoryName)
					.withRequest(request)
					.build();
		}
	}

	/**
	 * A {@link SCMNavigatorRequest.Witness} that counts how many sources have been observed.
	 */
	private static class WitnessImpl implements SCMNavigatorRequest.Witness {
		/**
		 * The count of repositories matches.
		 */
		@GuardedBy("this")
		private int count;

		/**
		 * The listener to log to.
		 */
		private final TaskListener listener;

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
}
