package org.jenkinsci.plugins.tuleap_git_branch_source;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.mixin.ChangeRequestSCMRevision;
import org.jetbrains.annotations.NotNull;

public class TuleapPullRequestRevision extends ChangeRequestSCMRevision<TuleapPullRequestSCMHead> {

    private final TuleapBranchSCMRevision origin;
    private final String targetHash;

    public TuleapPullRequestRevision(@NotNull TuleapPullRequestSCMHead head, @NotNull TuleapBranchSCMRevision target, @NonNull TuleapBranchSCMRevision origin) {
        super(head, target);
        this.origin = origin;
        this.targetHash = target.getHash();
    }

    @Override
    public boolean equivalent(ChangeRequestSCMRevision<?> revision) {
        if(!(revision instanceof TuleapPullRequestRevision)){
            return false;
        }
        TuleapPullRequestRevision tlpRevision = (TuleapPullRequestRevision) revision;
        return this.origin.equals(tlpRevision.getOrigin());
    }

    @Override
    protected int _hashCode() {
        return this.origin.hashCode();
    }

    public SCMRevision getOrigin() {
        return this.origin;
    }

    public String getTargetHash() {
        return this.targetHash;
    }
}
