package com.francetelecom.faas.jenkinsfaasbranchsource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.DataBoundConstructor;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.francetelecom.faas.jenkinsfaasbranchsource.config.OrangeForgeSettings;
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFGitRepository;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.RestrictedSince;
import hudson.model.Action;
import hudson.model.TaskListener;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMNavigatorDescriptor;
import jenkins.scm.api.SCMNavigatorEvent;
import jenkins.scm.api.SCMNavigatorOwner;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCategory;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.trait.SCMNavigatorRequest;
import jenkins.scm.api.trait.SCMTrait;
import jenkins.scm.impl.UncategorizedSCMSourceCategory;
import net.jcip.annotations.GuardedBy;

public class OFSCMNavigator extends SCMNavigator {

	private final String projectId;
	private List<SCMTrait<? extends SCMTrait>> traits;
	private String credentialsId;


	@DataBoundConstructor
	public OFSCMNavigator(String projectId) {
		this.projectId = projectId;
		this.traits = new ArrayList<>();
	}

	@Override
	protected String id() {
		return "https://www.forge.orange-labs.fr/projects/" + projectId;
	}

	@Override
	public void visitSources(SCMSourceObserver observer) throws IOException, InterruptedException {
		TaskListener listener = observer.getListener();

		if (StringUtils.isBlank(projectId)) {
			listener.getLogger().format("Must specify a project Id%n");
			return;
		}
		listener.getLogger().printf("Visit Sources of %s...%n", getprojectId());

		final OrangeForgeSettings orangeForgeSettings = new OrangeForgeSettings();
		StandardUsernamePasswordCredentials credentials = orangeForgeSettings.credentials();
		OFClient client = new OFClient(orangeForgeSettings);
		client.projectRepositories();

		try (final OFSCMNavigatorRequest request = new OFSCMNavigatorContext()
				.withTraits(traits)
				.newRequest(this, observer)) {
			SourceFactory sourceFactory = new SourceFactory(request);
			WitnessImpl witness = new WitnessImpl(listener);
			for (OFGitRepository repo : client.projectRepositories()) {
				if (request.process(repo.getName(), sourceFactory, null, witness)) {
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

		actions.add(new OFProjectMetadataAction(client.projectById(projectId)));
		return actions;
	}

	/**
	 * Gets the Id of the project who's repositories will be navigated.
	 * @return the Idof the project who's repositories will be navigated.
	 */
	public String getprojectId() {
		return projectId;
	}

	@Symbol("orangeforge")
	@Extension
	public static class DescriptorImpl extends SCMNavigatorDescriptor {

		private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());

		@Deprecated
		@Restricted(DoNotUse.class)
		@RestrictedSince("2.2.0")
		public static final String defaultIncludes = "*";
		@Deprecated
		@Restricted(DoNotUse.class)
		@RestrictedSince("2.2.0")
		public static final String defaultExcludes = "";

		//@Inject private OFSCMSource.DescriptorImpl delegate;


		@Override
		public String getPronoun() {
			return Messages.OFSCMNavigator_Pronoun();
		}

		@Override
		public SCMNavigator newInstance(@CheckForNull String projectId) {
			OFSCMNavigator navigator = new OFSCMNavigator(projectId);
			//navigator.setTraits(getTraitsDefaults());
			return navigator;
		}


		@Override
		public String getDisplayName() {
			return "OrangeForge Project";
		}

		protected SCMSourceCategory[] createCategories() {
			return new SCMSourceCategory[]{
					new UncategorizedSCMSourceCategory(Messages._OFSCMNavigator_DepotSourceCategory())
			};
		}
	}

	private static class SourceFactory implements SCMNavigatorRequest.SourceLambda {

		private final OFSCMNavigatorRequest request;

		public SourceFactory(OFSCMNavigatorRequest request) {
			this.request = request;
		}

		@NonNull
		@Override
		public SCMSource create(@NonNull String projectName) throws IOException, InterruptedException {
			//TODO
			//return new OFSCMSource(projectName, "the repo");
			return new OFSCMSource();
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
