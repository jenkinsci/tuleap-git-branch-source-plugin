package org.jenkinsci.plugins.tuleap_git_branch_source;

import io.jenkins.plugins.tuleap_api.client.GitHead;
import io.jenkins.plugins.tuleap_api.client.GitPullRequest;
import io.jenkins.plugins.tuleap_api.client.GitRepositoryReference;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.mixin.ChangeRequestSCMRevision;
import org.jenkinsci.plugins.tuleap_git_branch_source.stubs.SomeChangeRequestRevision;
import org.jenkinsci.plugins.tuleap_git_branch_source.stubs.SomeChangeRequestSCMHead;
import org.junit.Test;

import static org.junit.Assert.*;

public class TuleapPullRequestRevisionTest {

    @Test
    public void itReturnsFalseIfTheRevisionIsNotATuleapPRRevision() {
        TuleapBranchSCMHead targetBranch = new TuleapBranchSCMHead("targeto-branchu");
        TuleapBranchSCMRevision targetBranchRevision = new TuleapBranchSCMRevision(targetBranch, "t4rg3t_h4sh");

        TuleapBranchSCMHead originBranch = new TuleapBranchSCMHead("origino-branchu");
        TuleapBranchSCMRevision originBranchRevision = new TuleapBranchSCMRevision(originBranch, "0r1g4n_h4sh");

        TuleapPullRequestSCMHead pullRequestSCMHead = new TuleapPullRequestSCMHead(this.getGitPullRequest(), SCMHeadOrigin.DEFAULT, targetBranch, 101, 102, "refs/tlpr/4");

        TuleapPullRequestRevision tuleapPullRequestRevision = new TuleapPullRequestRevision(pullRequestSCMHead, targetBranchRevision, originBranchRevision);

        assertFalse(tuleapPullRequestRevision.equivalent(this.getNonTuleapChangeRequestRevision(targetBranch.getName())));
    }

    @Test
    public void itReturnsTrueIfTheGivenRevisionIsTheWantedRevision() {
        TuleapBranchSCMHead targetBranch = new TuleapBranchSCMHead("targeto-branchu");
        TuleapBranchSCMRevision targetBranchRevision = new TuleapBranchSCMRevision(targetBranch, "t4rg3t_h4sh");

        TuleapBranchSCMHead originBranch = new TuleapBranchSCMHead("origino-branchu");
        TuleapBranchSCMRevision originBranchRevision = new TuleapBranchSCMRevision(originBranch, "0r1g4n_h4sh");

        TuleapPullRequestSCMHead pullRequestSCMHead = new TuleapPullRequestSCMHead(this.getGitPullRequest(), SCMHeadOrigin.DEFAULT, targetBranch, 101, 101, "refs/tlpr/4");

        TuleapPullRequestRevision tuleapPullRequestRevision = new TuleapPullRequestRevision(pullRequestSCMHead, targetBranchRevision, originBranchRevision);

        assertTrue(tuleapPullRequestRevision.equivalent(tuleapPullRequestRevision));
    }

    @Test
    public void itReturnsFalseIfTheGivenRevisionIsNotTheWantedRevision() {
        TuleapBranchSCMHead targetBranch = new TuleapBranchSCMHead("targeto-branchu");
        TuleapBranchSCMRevision targetBranchRevision = new TuleapBranchSCMRevision(targetBranch, "t4rg3t_h4sh");

        TuleapBranchSCMHead originBranch = new TuleapBranchSCMHead("origino-branchu");
        TuleapBranchSCMRevision originBranchRevision = new TuleapBranchSCMRevision(originBranch, "0r1g4n_h4sh");

        TuleapPullRequestSCMHead pullRequestSCMHead = new TuleapPullRequestSCMHead(this.getGitPullRequest(), SCMHeadOrigin.DEFAULT, targetBranch, 101, 102, "refs/tlpr/4");

        TuleapPullRequestRevision tuleapPullRequestRevision = new TuleapPullRequestRevision(pullRequestSCMHead, targetBranchRevision, originBranchRevision);

        TuleapBranchSCMHead otherTargetBranch = new TuleapBranchSCMHead("targeto-0th3r-branchu");
        TuleapBranchSCMRevision otherTargetBranchRevision = new TuleapBranchSCMRevision(otherTargetBranch, "t4rg3t_h4sh_-0th3r");

        TuleapBranchSCMHead otherOriginBranch = new TuleapBranchSCMHead("origino-0th3r-branchu");
        TuleapBranchSCMRevision otherOriginBranchRevision = new TuleapBranchSCMRevision(otherOriginBranch, "0r1g4n_h4sh_-0th3r");

        TuleapPullRequestSCMHead otherPullRequestSCMHead = new TuleapPullRequestSCMHead(this.getGitPullRequest(), SCMHeadOrigin.DEFAULT, otherTargetBranch, 101, 102, "refs/tlpr/4");

        TuleapPullRequestRevision otherPullRequestRevision = new TuleapPullRequestRevision(otherPullRequestSCMHead, otherTargetBranchRevision,otherOriginBranchRevision);
        assertFalse(tuleapPullRequestRevision.equivalent(otherPullRequestRevision));
    }

    private ChangeRequestSCMRevision<?> getNonTuleapChangeRequestRevision(String headName) {
        SomeChangeRequestSCMHead headTarget = new SomeChangeRequestSCMHead(headName);
        SCMRevision target = new AbstractGitSCMSource.SCMRevisionImpl(new SCMHead(headName), headName);
        return new SomeChangeRequestRevision(headTarget, target);
    }


    private GitPullRequest getGitPullRequest() {
        return new GitPullRequest() {
            @Override
            public String getId() {
                return null;
            }

            @Override
            public GitRepositoryReference getSourceRepository() {
                return null;
            }

            @Override
            public GitRepositoryReference getDestinationRepository() {
                return null;
            }

            @Override
            public String getSourceBranch() {
                return null;
            }

            @Override
            public String getDestinationBranch() {
                return null;
            }

            @Override
            public GitHead getHead() {
                return null;
            }
        };
    }

}
