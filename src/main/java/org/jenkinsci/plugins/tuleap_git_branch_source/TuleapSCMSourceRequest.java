package org.jenkinsci.plugins.tuleap_git_branch_source;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMSourceRequest;

public class TuleapSCMSourceRequest extends SCMSourceRequest {

    /**
     * {@code true} if branch details need to be fetched.
     */
    private final boolean fetchBranches;

    protected TuleapSCMSourceRequest(@NonNull SCMSource source, @NonNull TuleapSCMSourceContext context,
                                     @CheckForNull TaskListener listener) {
        super(source, context, listener);

        fetchBranches = context.wantBranches();
    }
    public boolean isFetchBranches() {
        return fetchBranches;
    }
}
