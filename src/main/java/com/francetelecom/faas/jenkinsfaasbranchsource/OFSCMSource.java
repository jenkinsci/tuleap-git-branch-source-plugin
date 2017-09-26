package com.francetelecom.faas.jenkinsfaasbranchsource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.francetelecom.faas.jenkinsfaasbranchsource.config.OrangeForgeSettings;
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFGitBranch;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.trait.SCMSourceTrait;

/**
 * SCM source implementation for OrangeForge
 * discover branch af a repo
 */
public class OFSCMSource extends SCMSource {

	/**
	 * OrangeForge project to build URL from.
	 */
	/*private final String project;


	*//**
	 * Git Repository name to build URL from.
	 *//*
	private final String repository;

	*//**
	 * The behaviours to apply to this source.
	 */
	private List<SCMSourceTrait> traits = new ArrayList<>();

	/*//TO be used in SCM Navigator
	//TODO
	@DataBoundConstructor
	public OFSCMSource(String project, String repository) {
		this.project = project;
		this.repository = repository;
	}*/

	@Override
	protected void retrieve(@CheckForNull SCMSourceCriteria criteria,
							@NonNull SCMHeadObserver observer,
							@CheckForNull SCMHeadEvent<?> event,
							@NonNull TaskListener listener) throws IOException, InterruptedException {
		//final OFClient =
		try (final OFSCMSourceRequest request = new OFSCMSourceContext(criteria, observer)
				.withTraits(traits)
				.newRequest(this, listener)) {
			StandardUsernamePasswordCredentials credentials = new OrangeForgeSettings().credentials();
			request.listener().getLogger().print("");
			if (request.isFetchBranches()) {
				OFClient client = new OFClient(new OrangeForgeSettings());
				final List<OFGitBranch> branches = client.branchByGitRepo("//TODO autoretrieve repo from here");
				request.setBranches(branches);
				int count=0;
				for (OFGitBranch branch : branches) {
					count++;
					/*if (request.process(new OFBranchSCMHead(branch.getName()),
										branch::getSha1,
										new OFProbeFactory(client, request),
										new OFRevisionFactory(),
										new OFWitness())) {
						request.listener().getLogger().format("%n  %d branches were processed (query completed)%n", count);
					}*/

				}

			}
		}
	}

	/*@NonNull
	@Override
	protected SCMProbe createProbe(@NonNull SCMHead head, @CheckForNull SCMRevision revision) throws IOException {
		return null;
	}*/

	@Override
	public SCM build(@NonNull SCMHead scmHead,
					 @CheckForNull SCMRevision scmRevision) {
		//String ref = scmHead.getName();
		//TODO
		return new GitSCM("the repo");
	}

	/*public List<SCMSourceTrait> getTraits() {
		return Collections.unmodifiableList(traits);
	}

	@DataBoundSetter
	public void setTraits(List<SCMSourceTrait> traits) {
		this.traits = new ArrayList<>(Util.fixNull(traits));
	}

	//TODO
	//@Symbol("orangeforge")
	@Extension
	public static class DescriptorImpl extends SCMSourceDescriptor {

		@Override
		public String getDisplayName() {
			return "OrangeForge";
		}
	}

	private static class OFRevisionFactory  implements SCMSourceRequest.LazyRevisionLambda<SCMHead, SCMRevision,
			String> {
		@NonNull
		@Override
		public SCMRevision create(@NonNull SCMHead scmHead, @Nullable String sha1) throws IOException,
				InterruptedException {
			return new AbstractGitSCMSource.SCMRevisionImpl(scmHead, sha1);
		}
	}

	private static class OFWitness implements SCMSourceRequest.Witness {

		@Override
		public void record(@NonNull SCMHead scmHead, @CheckForNull SCMRevision revision, boolean isMatch) {
			//TODO
		}
	}

	private static class OFProbeFactory implements SCMSourceRequest.ProbeLambda<SCMHead, String> {
		private OFProbeFactory(OFClient ofClient, OFSCMSourceRequest request) {
		}

		@NonNull
		@Override
		public SCMSourceCriteria.Probe create(@NonNull SCMHead head, @Nullable String sha1) throws IOException,
				InterruptedException {
			//TODO
			return new SCMSourceCriteria.Probe() {
				@Override
				public String name() {
					return head.getName();
				}

				@Override
				public long lastModified() {
					//TODO find commit date of sha1
					return 0;
				}

				@Override
				public boolean exists(@NonNull String path) throws IOException {
					//TODO check path exists
					return false;
				}
			};
		}
	}*/
}
