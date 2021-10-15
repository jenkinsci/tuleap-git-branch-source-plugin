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
    private final Integer targetRepositoryId;
    private final String headReference;

    public TuleapPullRequestSCMHead(GitPullRequest pullRequest, SCMHeadOrigin origin, TuleapBranchSCMHead target, Integer originRepositoryId, Integer targetRepositoryId, String headReference) {
        super("(" + pullRequest.getId() + ")" + pullRequest.getTitle());
        this.pullRequest = pullRequest;
        this.origin = origin;
        this.target = target;
        this.originRepositoryId = originRepositoryId;
        this.targetRepositoryId = targetRepositoryId;
        this.headReference = headReference;
    }

    @NotNull
    @Override
    public ChangeRequestCheckoutStrategy getCheckoutStrategy() {
        return ChangeRequestCheckoutStrategy.HEAD;
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

    public Integer getTargetRepositoryId() {
        return this.targetRepositoryId;
    }

    public String getHeadReference() {
        return this.headReference;
    }
}
