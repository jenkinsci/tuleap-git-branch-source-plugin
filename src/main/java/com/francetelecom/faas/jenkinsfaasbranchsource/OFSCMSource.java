package com.francetelecom.faas.jenkinsfaasbranchsource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.transport.RefSpec;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.francetelecom.faas.jenkinsfaasbranchsource.config.OrangeForgeSettings;
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFGitBranch;
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFGitCommit;
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFGitRepository;
import com.francetelecom.faas.jenkinsfaasbranchsource.trait.BranchDiscoveryTrait;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.Util;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.TaskListener;
import hudson.model.queue.Tasks;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.impl.trait.WildcardSCMHeadFilterTrait;

/**
 * SCM source implementation for OrangeForge
 * discover branch af a repo
 */
public class OFSCMSource extends AbstractGitSCMSource {

	private static final Logger LOGGER = LoggerFactory.getLogger(OFSCMSource.class);

	/**
	 * OrangeForge project to build URL from.
	 */
	private final String projectId;

	private static String includes = "*";
	private static String excludes = "";

	private WildcardSCMHeadFilterTrait wildcardTrait;

	/**
	 * Git Repository path to build URL from.
	 */
	private final String repositoryPath;

	/**
	 * The behaviours to apply to this source.
	 */
	private List<SCMSourceTrait> traits = new ArrayList<>();
	private String credentialsId;

	@DataBoundConstructor
	public OFSCMSource(String id, String projectId, String repositoryPath) {
		//TODO remove call to super(..) when moving to 2.60 with org.jenkins-ci.plugins.git:3.5.0
		super(id);
		this.projectId = projectId;
		this.repositoryPath = repositoryPath;
		this.wildcardTrait = new WildcardSCMHeadFilterTrait(includes, excludes);
		traits.add(wildcardTrait);
		traits.add(new BranchDiscoveryTrait());
	}

	@Override
	protected void retrieve(@CheckForNull SCMSourceCriteria criteria,@NonNull SCMHeadObserver observer,
							@CheckForNull SCMHeadEvent<?> event, @NonNull TaskListener listener)
			throws IOException, InterruptedException {
		try (final OFSCMSourceRequest request = new OFSCMSourceContext(criteria, observer)
				.withTraits(traits)
				.newRequest(this, listener)) {
			StandardUsernamePasswordCredentials credentials = new OrangeForgeSettings().credentials();
			request.listener().getLogger().print("");
			if (request.isFetchBranches()) {
				OFClient client = new OFClient(new OrangeForgeSettings());
				LOGGER.info("Fecthing branches for repository at %s", repositoryPath);
				final List<OFGitBranch> branches = client.branchByGitRepo(repositoryPath);
				request.setBranches(branches);
				int count=0;
				for (OFGitBranch branch : branches) {
					count++;
					if (request.process(new OFBranchSCMHead(branch.getName()),
										branch::getSha1,
										new OFProbeFactory(client, request, repositoryPath),
										new OFRevisionFactory(),
										new OFWitness(listener))) {
						request.listener().getLogger().format("%n  %d branches were processed (query completed)%n", count);
					}

				}

			}
		}
	}

	@Override
	protected List<RefSpec> getRefSpecs() {
		return null;
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


	@Override
	public SCM build(@NonNull SCMHead scmHead,
					 @CheckForNull SCMRevision scmRevision) {
		return new OFSCMBuilder(scmHead, scmRevision, repositoryPath).withTraits(traits).build();
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
		return repositoryPath;
	}

	@Override
	public String getIncludes() {
		return wildcardTrait.getIncludes();
	}

	@Override
	public String getExcludes() {
		return wildcardTrait.getExcludes();
	}

	/**
	 * Gets the credentials used to access the OrangeForge REST API (also used as the default credentials for checking
	 * out
	 * sources.
	 * @return the credentials used to access the OrangeForge REST API
	 */
	@Override
	@CheckForNull
	public String getCredentialsId() {
		return credentialsId;
	}


	/**
	 * Sets the credentials used to access the OrangeForge REST API (also used as the default credentials for checking out
	 * sources.
	 *
	 * @param credentialsId the credentials used to access the OrangeForge REST API
	 * @since 2.2.0
	 */
	@DataBoundSetter
	public void setCredentialsId(String credentialsId) {
		this.credentialsId = credentialsId;
	}

	@Symbol("orangeforge")
	@Extension
	public static class DescriptorImpl extends SCMSourceDescriptor {


		@Override
		public String getDisplayName() {
			return "OrangeForge";
		}

		public List<SCMSourceTrait> getTraitsDefaults() {
			return Arrays.asList(new BranchDiscoveryTrait(), new WildcardSCMHeadFilterTrait(includes, excludes));
		}

		public ListBoxModel doFillCredentialsIdItems(@CheckForNull @AncestorInPath Item context,
													 @QueryParameter String apiUri,
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
							URIRequirementBuilder.fromUri(StringUtils.defaultIfEmpty(apiUri, "https://www.forge.orange-labs.fr/api"))
												 .build(),
							CredentialsMatchers.anyOf(CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class))
					);
		}

		public ListBoxModel doFillApiUriItems() {
			ListBoxModel listBox = new ListBoxModel();
			listBox.add("OrangeForge", "");
			listBox.add("OrangeForge API", "https://www.forge.orange-labs.fr/api");
			return listBox;
		}

		public boolean isApiUriSelectable() {
			return true;
		}

		@RequirePOST
		public ListBoxModel doFillRepositoryItems(@CheckForNull @AncestorInPath Item context, @QueryParameter String apiUri,
												  @QueryParameter String credentialsId, @QueryParameter String repoOwner) throws IOException {
			ListBoxModel model = new ListBoxModel();
			//TODO refactor also in SCMNavigator
			final OrangeForgeSettings orangeForgeSettings = new OrangeForgeSettings();
			StandardUsernamePasswordCredentials credentials = orangeForgeSettings.credentials();
			OFClient client = new OFClient(orangeForgeSettings);
			for (OFGitRepository repo : client.projectRepositories()) {
				model.add(repo.getName());
			}
			return model;
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

	private static class OFProbeFactory implements SCMSourceRequest.ProbeLambda<SCMHead, String> {

		private final OFClient ofClient;
		private final OFSCMSourceRequest request;
		private final String repoPath;

		public OFProbeFactory(OFClient ofClient, OFSCMSourceRequest request, String repoPath) {
			this.ofClient = ofClient;
			this.request = request;
			this.repoPath = repoPath;
		}

		@NonNull
		@Override
		public SCMSourceCriteria.Probe create(@NonNull SCMHead head, @Nullable String sha1) throws IOException,
				InterruptedException {
			return new SCMSourceCriteria.Probe() {
				@Override
				public String name() {
					return head.getName();
				}

				@Override
				public long lastModified() {
						OFGitCommit commit = ofClient.resolveCommit(sha1, repoPath);
						if (commit == null) {
							request.listener().getLogger()
								   .format("Can not resolve commit by hash [%s] on repository %s/%s%n",
										   sha1, repoPath, repoPath);
							return 0;
						}
						return commit.getDateMillis();
				}

				@Deprecated
				@Override
				public boolean exists(@NonNull String path) throws IOException {
					return true;
				}
			};
		}
	}
}
