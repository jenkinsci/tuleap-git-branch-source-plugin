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
import com.francetelecom.faas.jenkinsfaasbranchsource.config.OFConfiguration;
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFGitBranch;
import com.francetelecom.faas.jenkinsfaasbranchsource.trait.BranchDiscoveryTrait;

import static com.francetelecom.faas.jenkinsfaasbranchsource.config.OFConnector.checkCredentials;
import static com.francetelecom.faas.jenkinsfaasbranchsource.config.OFConnector.listScanCredentials;
import static com.francetelecom.faas.jenkinsfaasbranchsource.config.OFConnector.lookupScanCredentials;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
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
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.api.trait.SCMSourceTrait;

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

	private String apiBaseUri;
	private String gitBaseUri;

	/**
	 * Git Repository path to build URL from.
	 */
	private final String repositoryPath;

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
	public OFSCMSource(final String projectId, final String
			repositoryPath) {
		this.projectId = projectId;
		this.repositoryPath = repositoryPath;
		traits.add(new BranchDiscoveryTrait());
	}

	@Override
	protected void retrieve(@CheckForNull SCMSourceCriteria criteria,@NonNull SCMHeadObserver observer,
							@CheckForNull SCMHeadEvent<?> event, @NonNull TaskListener listener)
			throws IOException, InterruptedException {
		try (final OFSCMSourceRequest request = new OFSCMSourceContext(criteria, observer)
				.withTraits(traits)
				.wantBranches(true)
				.newRequest(this, listener)) {
			StandardCredentials credentials = lookupScanCredentials((Item) getOwner(), getApiBaseUri(), getCredentialsId());
			setCredentials(credentials);
			setRemoteUrl(getGitBaseUri() + repositoryPath);
			if (request.isFetchBranches()) {
				OFClient client = new OFClient(credentials, getApiBaseUri(), getGitBaseUri());
				LOGGER.info("Fecthing branches for repository at {}", repositoryPath);
				final List<OFGitBranch> branches = client.branchByGitRepo(repositoryPath);
				request.setBranches(branches);
				int count=0;
				for (OFGitBranch branch : branches) {
					count++;
					request.listener().getLogger().format("Crawling branch %s::%s for repo %s", branch.getName(),
														 branch.getSha1(), getRemote()).println();
					OFBranchSCMHead head = new OFBranchSCMHead(branch.getName());
					if (request.process(head, new SCMRevisionImpl(head, branch.getSha1()),
										OFSCMSource.this::fromSCMFileSystem,
										new OFWitness(listener))) {
						request.listener().getLogger().format("%n  %d branches were processed (query completed)%n",
															  count).println();
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
	public SCM build(@NonNull SCMHead scmHead,
					 @CheckForNull SCMRevision scmRevision) {
		//TODO check credentialsId is propagated from Navigator to here and to GtiSCM so it can perform clone
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

	public String getApiBaseUri() {
		if (StringUtils.isBlank(apiBaseUri)){
			apiBaseUri = OFConfiguration.get().getApiBaseUrl();
		}
		return apiBaseUri;
	}

	public String getGitBaseUri() {
		if (StringUtils.isBlank(gitBaseUri)){
			gitBaseUri = OFConfiguration.get().getGitBaseUrl();
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
		public FormValidation doCheckCredentialsId( @AncestorInPath Item item, @QueryParameter String value,
													@CheckForNull @AncestorInPath Item context,  @QueryParameter
															String apiUri, @QueryParameter String credentialsId ) {
			return checkCredentials(item, apiUri);
		}

		public ListBoxModel doFillCredentialsIdItems(@CheckForNull @AncestorInPath Item context,
													 @QueryParameter String apiUri,
													 @QueryParameter String credentialsId) {
			return listScanCredentials(context, apiUri, credentialsId);
		}

		@RequirePOST
		public ListBoxModel doFillRepositoryItems(@CheckForNull @AncestorInPath Item context, @QueryParameter String
				projectId, @QueryParameter String credentialsId, @QueryParameter String repoOwner) throws IOException {
			ListBoxModel model = new ListBoxModel();
			final String apiBaseUrl = OFConfiguration.get().getApiBaseUrl();
			StandardCredentials credentials = lookupScanCredentials(context, apiBaseUrl, credentialsId);
			OFClient client = new OFClient(credentials, apiBaseUrl, OFConfiguration.get().getGitBaseUrl());
			client.projectRepositories(projectId).stream().distinct().forEach(ofGitRepository -> model.add(ofGitRepository.getName()));
			return model;
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
