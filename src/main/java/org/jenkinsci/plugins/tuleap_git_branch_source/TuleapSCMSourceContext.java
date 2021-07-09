package org.jenkinsci.plugins.tuleap_git_branch_source;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.trait.SCMSourceContext;

public class TuleapSCMSourceContext extends SCMSourceContext<TuleapSCMSourceContext, TuleapSCMSourceRequest> {

    /**
     * {@code true} if the {@link TuleapSCMSourceRequest} will need information about branches.
     */
    private boolean wantBranches = false;

    private boolean notifyPullRequest = false;

    private boolean wantPullRequests = false;

    public TuleapSCMSourceContext(@CheckForNull SCMSourceCriteria criteria, @NonNull SCMHeadObserver observer) {
        super(criteria, observer);
    }

    @NonNull
    @Override
    public TuleapSCMSourceRequest newRequest(@NonNull SCMSource scmSource, @CheckForNull TaskListener taskListener) {
        return new TuleapSCMSourceRequest(scmSource, this, taskListener);
    }

    /**
     * Returns {@code true} if the {@link TuleapSCMSourceRequest} will need information about branches.
     */
    public final boolean wantBranches() {
        return wantBranches;
    }

    public final boolean isNotifyPullRequest() {
        return this.notifyPullRequest;
    }

    public final boolean wantPullRequests() {return this.wantPullRequests; }

    /**
     * Adds a requirement for branch details to any {@link TuleapSCMSourceContext} for this context.
     *
     * @param include
     *            {@code true} to add the requirement or {@code false} to leave the requirement as is (makes simpler
     *            with method chaining)
     * @return {@code this} for method chaining.
     */
    @NonNull
    public TuleapSCMSourceContext wantBranches(boolean include) {
        wantBranches = wantBranches || include;
        return this;
    }

    @NonNull
    public TuleapSCMSourceContext notifyPullRequest(boolean notify) {
        this.notifyPullRequest = this.notifyPullRequest || notify;
        return this;
    }

    @NonNull
    public TuleapSCMSourceContext wantPullRequests(boolean include) {
        this.wantPullRequests = this.wantPullRequests || include;
        return this;
    }

}
