package org.jenkinsci.plugins.tuleap_git_branch_source;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.tuleap_api.client.GitPullRequest;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.api.mixin.ChangeRequestSCMHead2;
import org.jetbrains.annotations.NotNull;

public class TuleapPullRequestSCMHead extends SCMHead implements ChangeRequestSCMHead2 {

    private final GitPullRequest pullRequest;
    private final SCMHeadOrigin origin;
    private final TuleapBranchSCMHead target;
    private final int originRepositoryId;

    public TuleapPullRequestSCMHead(GitPullRequest pullRequest, SCMHeadOrigin origin, TuleapBranchSCMHead target, Integer originRepositoryId) {
        super("TLP-PR-" + pullRequest.getId());
        this.pullRequest = pullRequest;
        this.origin = origin;
        this.target = target;
        this.originRepositoryId = originRepositoryId;
    }

    @NotNull
    @Override
    public ChangeRequestCheckoutStrategy getCheckoutStrategy() {
        return ChangeRequestCheckoutStrategy.MERGE;
    }

    @NotNull
    @Override
    public String getOriginName() {
        return this.pullRequest.getSourceBranch();
    }

    @NotNull
    @Override
    public String getId() {
        return this.pullRequest.getId();
    }

    @NonNull
    @Override
    public SCMHeadOrigin getOrigin() {
        return this.origin;
    }

    @NonNull
    @Override
    public SCMHead getTarget() {
        return this.target;
    }

    public int getOriginRepositoryId() {
        return this.originRepositoryId;
    }
}
