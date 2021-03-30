package org.jenkinsci.plugins.tuleap_git_branch_source;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.api.mixin.ChangeRequestSCMHead2;
import org.jetbrains.annotations.NotNull;

/**
 * Head corresponding to a branch of an Tuleap git repository.
 */
public class TuleapPullRequestSCMHead extends SCMHead implements ChangeRequestSCMHead2 {
    private static final Long serialVersionUID = 1L;

    private final String id;
    private final String originName;
    private final TuleapBranchSCMHead target;

    /**
     * {@inheritDoc}
     */
    public TuleapPullRequestSCMHead(
        @NonNull String id,
        @NonNull String name,
        @NonNull String branchSrc,
        @NonNull String head
    ) {
        super(name);
        this.id = id;
        this.originName = branchSrc;
        this.target = new TuleapBranchSCMHead(head);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPronoun() {
        return Messages.PullRequestSCMHead_pronoun();
    }

    @NotNull
    @Override
    public ChangeRequestCheckoutStrategy getCheckoutStrategy() {
        return ChangeRequestCheckoutStrategy.HEAD;
    }

    @NotNull
    @Override
    public String getOriginName() {
        return this.originName;
    }

    @NotNull
    @Override
    public String getId() {
        return id;
    }

    @NotNull
    @Override
    public SCMHead getTarget() {
        return this.target;
    }
}
