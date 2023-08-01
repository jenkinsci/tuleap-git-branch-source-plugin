package org.jenkinsci.plugins.tuleap_git_branch_source;

import hudson.model.Action;
import hudson.model.Actionable;
import io.jenkins.plugins.tuleap_api.deprecated_client.api.TuleapGitRepository;
import jenkins.scm.api.SCMHeadOrigin;
import org.jenkinsci.plugins.tuleap_git_branch_source.stubs.GitPullRequestStub;
import org.jenkinsci.plugins.tuleap_git_branch_source.stubs.TuleapActionableStub;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class TuleapRepositoryActionBuilderTest {

    @Test
    public void testItReturnsTheGitRepositoryLink() {
        Actionable owner = TuleapActionableStub.withDefaultTuleapLink();
        TuleapBranchSCMHead branch = new TuleapBranchSCMHead("dev");
        TuleapGitRepository gitRepository = new TuleapGitRepository();
        gitRepository.setName("dev");
        String projectId = "101";
        String gitBaseUri = "https://tuleap-git-base.example.com";

        List<Action> result = TuleapRepositoryActionBuilder.buildTuleapRepositoryActions(
            owner,
            branch,
            gitRepository,
            projectId,
            gitBaseUri
        );

        TuleapLink expectedLink = new TuleapLink(
            "icon-git-branch",
            "https://tuleap-git-link.example.com?a=shortlog&h=dev"
        );
        assertEquals(expectedLink.getIconClassName(), ((TuleapLink) result.get(0)).getIconClassName());
        assertEquals(expectedLink.getUrlName(), result.get(0).getUrlName());
    }

    @Test
    public void testItReturnsTheGitPullRequestLink() {
        Actionable owner = TuleapActionableStub.withDefaultTuleapLink();
        TuleapBranchSCMHead targetBranch = new TuleapBranchSCMHead("targeto-branchu");
        TuleapPullRequestSCMHead pullRequestSCMHead = new TuleapPullRequestSCMHead(GitPullRequestStub.withId("15"), SCMHeadOrigin.DEFAULT, targetBranch, 101, 101, "refs/tlpr/4");

        TuleapGitRepository gitRepository = new TuleapGitRepository();
        gitRepository.setName("dev");
        String projectId = "101";
        String gitBaseUri = "https://tuleap-git-base.example.com";

        List<Action> result = TuleapRepositoryActionBuilder.buildTuleapRepositoryActions(
            owner,
            pullRequestSCMHead,
            gitRepository,
            projectId,
            gitBaseUri
        );

        TuleapLink expectedLink = new TuleapLink(
            "icon-git-branch",
            "https://tuleap-git-base.example.com?action=pull-requests&repo_id=0&group_id=101#/pull-requests/15/overview"
        );
        assertEquals(expectedLink.getIconClassName(), ((TuleapLink) result.get(0)).getIconClassName());
        assertEquals(expectedLink.getUrlName(), result.get(0).getUrlName());
    }

    @Test
    public void testItReturnsEmptyListIfTheCurrentSCMIsNotFromTuleap() {
        Actionable owner = TuleapActionableStub.withNull();

        TuleapBranchSCMHead branch = new TuleapBranchSCMHead("dev");
        TuleapGitRepository gitRepository = new TuleapGitRepository();
        gitRepository.setName("dev");
        String projectId = "101";
        String gitBaseUri = "https://tuleap-git-base.example.com";

        List<Action> result = TuleapRepositoryActionBuilder.buildTuleapRepositoryActions(
            owner,
            branch,
            gitRepository,
            projectId,
            gitBaseUri
        );
        assertTrue(result.isEmpty());
    }
}
