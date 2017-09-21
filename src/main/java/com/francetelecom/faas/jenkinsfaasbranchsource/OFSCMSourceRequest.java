package com.francetelecom.faas.jenkinsfaasbranchsource;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFGitBranch;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMSourceRequest;

public class OFSCMSourceRequest extends SCMSourceRequest{

	/**
	 * {@code true} if branch details need to be fetched.
	 */
	private final boolean fetchBranches;

	/**
	 * The branch details or {@code null} if not {@link #isFetchBranches()}.
	 */
	@CheckForNull
	private Iterable<OFGitBranch> branches;

	/**
	 * The set of origin branch names that the request is scoped to or {@code null} if the request is not limited.
	 */
	@CheckForNull
	private final Set<String> requestBranchNames;

	protected OFSCMSourceRequest(@NonNull SCMSource source,
								 @NonNull OFSCMSourceContext context,
								 @CheckForNull TaskListener listener) {
		super(source, context, listener);

		fetchBranches = context.wantBranches();
		Set<SCMHead> includes = context.observer().getIncludes();
		if (includes != null) {
			Set<String> branchNames = new HashSet<>(includes.size());
			for (SCMHead head : includes) {
				if (head instanceof OFBranchSCMHead) {
					branchNames.add(head.getName());
				}
			}
			this.requestBranchNames = Collections.unmodifiableSet(branchNames);
		} else {
			this.requestBranchNames = new HashSet<>();
		}
	}

	public boolean isFetchBranches() {
		return fetchBranches;
	}

	public Iterable<OFGitBranch> getBranches() {
		return branches;
	}

	public void setBranches(Iterable<OFGitBranch> branches) {
		this.branches = branches;
	}
}
