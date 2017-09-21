package com.francetelecom.faas.jenkinsfaasbranchsource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.lang.StringUtils;

import com.francetelecom.faas.jenkinsfaasbranchsource.config.OrangeForgeSettings;
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFGitRepository;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.TaskListener;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMNavigatorDescriptor;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCategory;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.trait.SCMNavigatorRequest;
import jenkins.scm.api.trait.SCMTrait;
import jenkins.scm.impl.UncategorizedSCMSourceCategory;
import net.jcip.annotations.GuardedBy;

public class OFSCMNavigator extends SCMNavigator {

	private final String project;
	private final List<SCMTrait<? extends SCMTrait>> traits;

	public OFSCMNavigator(String project) {
		this.project = project;
		this.traits = new ArrayList<>();
	}

	@Override
	protected String id() {
		//TODO
		return project;
	}

	@Override
	public void visitSources(SCMSourceObserver observer) throws IOException, InterruptedException {
		TaskListener listener = observer.getListener();

		if (StringUtils.isBlank(project)) {
			listener.getLogger().format("Must specify a project%n");
			return;
		}

		final OrangeForgeSettings orangeForgeSettings = new OrangeForgeSettings();
		//StandardCredentials credentials = orangeForgeSettings.credentials();
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

	//TODO
	//@Symbol("orangeforge")
	@Extension
	public static class DescriptorImpl extends SCMNavigatorDescriptor {

		@Override
		public SCMNavigator newInstance(@CheckForNull String s) {
			return null;
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
			return new OFSCMSource(projectName, "the repo");
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
