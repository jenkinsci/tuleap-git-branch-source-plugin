package org.jenkinsci.plugins.tuleap_git_branch_source;

import io.jenkins.plugins.tuleap_api.client.GitHead;
import io.jenkins.plugins.tuleap_api.client.GitPullRequest;
import io.jenkins.plugins.tuleap_api.client.GitRepositoryReference;
import jenkins.scm.api.SCMHeadOrigin;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


public class TuleapSCMBuilderTest {

    @Test
    public void testItCleansAndAddTheBranchRefSpecByDefault() {
        TuleapBranchSCMHead tlpScmHead = new TuleapBranchSCMHead("jcomprendspas-branch");
        TuleapBranchSCMRevision tlpRevision = new TuleapBranchSCMRevision(tlpScmHead, "h4sh_branch");
        String remote = "https://tuleap.example.com/repo.git";
        String credentialsId = "1581";

        TuleapSCMBuilder tuleapSCMBuilder = new TuleapSCMBuilder(tlpScmHead, tlpRevision, remote, credentialsId,"https://tuleap.example.com/plugins/git/somerepo");
        assertEquals(1, tuleapSCMBuilder.refSpecs().size());
        assertEquals("+refs/heads/jcomprendspas-branch:refs/remotes/@{remote}/jcomprendspas-branch", tuleapSCMBuilder.refSpecs().get(0));
        assertEquals("https://tuleap.example.com/repo.git", tuleapSCMBuilder.remote());
    }

    @Test
    public void testItCleansAndAddThePullRequestBranchRefSpecWhenThereIsAPullRequest() {
        TuleapBranchSCMHead tlpScmHeadTarget = new TuleapBranchSCMHead("naha");
        TuleapBranchSCMRevision tlpRevisionTarget = new TuleapBranchSCMRevision(tlpScmHeadTarget, "h4sh_t4rg3t");

        TuleapBranchSCMHead tlpScmHeadOrigin = new TuleapBranchSCMHead("tchiki-tchiki");
        TuleapBranchSCMRevision tlpRevisionOrigin = new TuleapBranchSCMRevision(tlpScmHeadOrigin, "h4shu_or1g1n");

        TuleapPullRequestSCMHead tlpPrScmHead = new TuleapPullRequestSCMHead(this.getPullRequest(), SCMHeadOrigin.DEFAULT, tlpScmHeadTarget,4,4, "refs/tlpr/4");
        TuleapPullRequestRevision tlpPrRevision = new TuleapPullRequestRevision(tlpPrScmHead, tlpRevisionTarget, tlpRevisionOrigin);

        String remote = "https://tuleap.example.com/repo.git";
        String credentialsId = "1581";

        TuleapSCMBuilder tuleapSCMBuilder = new TuleapSCMBuilder(tlpPrScmHead, tlpPrRevision, remote, credentialsId, "https://tuleap.example.com/plugins/git/somerepo");
        assertEquals(1, tuleapSCMBuilder.refSpecs().size());
        assertEquals("+refs/tlpr/4:refs/remotes/@{remote}/TLP-PR-3", tuleapSCMBuilder.refSpecs().get(0));
        assertEquals("https://tuleap.example.com/repo.git", tuleapSCMBuilder.remote());
    }

    @Test
    public void testItReturnsAGitSCMWithTheOriginRevisionWhenThereIsATuleapPullRequest() {
        TuleapPullRequestSCMHead tlpPrScmHead = mock(TuleapPullRequestSCMHead.class);
        when(tlpPrScmHead.getName()).thenReturn("some_branch");
        TuleapPullRequestRevision tlpPrRevision = mock(TuleapPullRequestRevision.class);

        String remote = "https://tuleap.example.com/repo.git";
        String credentialsId = "1581";

        TuleapSCMBuilder tuleapSCMBuilder = new TuleapSCMBuilder(tlpPrScmHead, tlpPrRevision, remote, credentialsId, "https://tuleap.example.com/plugins/git/somerepo");

        tuleapSCMBuilder.build();
        verify(tlpPrRevision, atLeastOnce()).getOrigin();
        assertEquals(tlpPrRevision, tuleapSCMBuilder.revision());
    }

    @Test
    public void testItReturnsAGitSCMWithTheBaseRevisionWhenThereIsNoTuleapPullRequest() {
        TuleapBranchSCMHead tlpScmHead = mock(TuleapBranchSCMHead.class);
        when(tlpScmHead.getName()).thenReturn("some_branch");
        TuleapBranchSCMRevision tlpRevision = mock(TuleapBranchSCMRevision.class);
        when(tlpRevision.getHash()).thenReturn("38dfa09a67d7872d821b6a46eff340bc8ae0af0f");
        when(tlpRevision.getHead()).thenReturn(tlpScmHead);

        String remote = "https://tuleap.example.com/repo.git";
        String credentialsId = "1581";

        TuleapSCMBuilder tuleapSCMBuilder = new TuleapSCMBuilder(tlpScmHead, tlpRevision, remote, credentialsId, "https://tuleap.example.com/plugins/git/somerepo");

        tuleapSCMBuilder.build();
        assertEquals(tlpRevision, tuleapSCMBuilder.revision());
    }

    private GitPullRequest getPullRequest() {
        return new GitPullRequest() {
            @Override
            public String getId() {
                return "3";
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
                return "naha";
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
