package com.francetelecom.faas.jenkinsfaasbranchsource;


import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.trait.SCMSourceContext;

public class OFSCMSourceContext extends SCMSourceContext<OFSCMSourceContext, OFSCMSourceRequest> {

	/**
	 * {@code true} if the {@link OFSCMSourceRequest} will need information about branches.
	 */
	private boolean wantBranches = false;

	public OFSCMSourceContext(@CheckForNull SCMSourceCriteria criteria, @NonNull SCMHeadObserver observer) {
		super(criteria, observer);
	}

	@NonNull
	@Override
	public OFSCMSourceRequest newRequest(@NonNull SCMSource scmSource, @CheckForNull TaskListener taskListener) {
		return new OFSCMSourceRequest(scmSource, this, taskListener);
	}

	/**
	 * Returns {@code true} if the {@link OFSCMSourceRequest} will need information about branches.
	 *
	 * @return {@code true} if the {@link OFSCMSourceRequest} will need information about branches.
	 */
	public final boolean wantBranches() {
		return wantBranches;
	}

	/**
	 * Adds a requirement for branch details to any {@link OFSCMSourceContext} for this context.
	 *
	 * @param include {@code true} to add the requirement or {@code false} to leave the requirement as is (makes
	 *                simpler with method chaining)
	 * @return {@code this} for method chaining.
	 */
	@NonNull
	public OFSCMSourceContext wantBranches (boolean include) {
		wantBranches = wantBranches || include;
		return this;
	}


}
