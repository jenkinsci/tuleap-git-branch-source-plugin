package org.jenkinsci.plugins.tuleap_branch_source;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMSourceRequest;
import org.jenkinsci.plugins.tuleap_branch_source.client.api.TuleapBranches;
import org.jenkinsci.plugins.tuleap_branch_source.client.api.TuleapGitBranch;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class TuleapSCMSourceRequest extends SCMSourceRequest {

    /**
     * {@code true} if branch details need to be fetched.
     */
    private final boolean fetchBranches;

    /**
     * The set of origin branch names that the request is scoped to or {@code null} if the request is not limited.
     */
    @CheckForNull
    private final Set<String> requestBranchNames;

    /**
     * The branch details or {@code null} if not {@link #isFetchBranches()}.
     */
    @CheckForNull
    private Stream<TuleapGitBranch> branches;

    private Stream<TuleapBranches> branchesFromTuleapApi;

    protected TuleapSCMSourceRequest(@NonNull SCMSource source, @NonNull TuleapSCMSourceContext context,
                                     @CheckForNull TaskListener listener) {
        super(source, context, listener);

        fetchBranches = context.wantBranches();
        Set<SCMHead> includes = context.observer().getIncludes();
        if (includes != null) {
            Set<String> branchNames = new HashSet<>(includes.size());
            for (SCMHead head : includes) {
                if (head instanceof TuleapBranchSCMHead) {
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

    public Stream<TuleapGitBranch> getBranches() {
        return branches;
    }

    public void setBranches(Stream<TuleapGitBranch> branches) {
        this.branches = branches;
    }

    public Stream<TuleapBranches> getBranchesFromTuleapApi() {
        return branchesFromTuleapApi;
    }

    public void setBranchesFromTuleapApi(Stream<TuleapBranches> branches){
        this.branchesFromTuleapApi = branches;
    }
}
