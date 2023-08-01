package org.jenkinsci.plugins.tuleap_git_branch_source;

import hudson.model.Action;
import hudson.model.Actionable;
import io.jenkins.plugins.tuleap_api.deprecated_client.api.TuleapGitRepository;
import jenkins.scm.api.SCMHead;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class TuleapRepositoryActionBuilder {
    public static List<Action> buildTuleapRepositoryActions(
        Actionable ownerAction,
        SCMHead head,
        TuleapGitRepository repository,
        String projectId,
        String gitBaseUri
    ) {
        List<Action> result = new ArrayList<>();
            TuleapLink repoLink = ownerAction.getAction(TuleapLink.class);
            if (repoLink != null) {
                if (head instanceof TuleapBranchSCMHead) {
                    String url = repoLink.getUrl() + "?a=shortlog&h=" + URLEncoder.encode(head.getName(), StandardCharsets.UTF_8);
                    result.add(new TuleapLink("icon-git-branch", url));
                } else if (head instanceof TuleapPullRequestSCMHead) {
                    TuleapPullRequestSCMHead tuleapPullRequestSCMHead = (TuleapPullRequestSCMHead) head;
                    String encodedRepositoryId = URLEncoder.encode(Integer.toString(repository.getId()), StandardCharsets.UTF_8);
                    String encodedPullRequestId = URLEncoder.encode(tuleapPullRequestSCMHead.getId(), StandardCharsets.UTF_8);
                    String encodedProjectId = URLEncoder.encode(projectId, StandardCharsets.UTF_8);
                    String prUrl = gitBaseUri+
                        "?action=pull-requests&repo_id=" + encodedRepositoryId +
                        "&group_id=" + encodedProjectId + "#/pull-requests/" + encodedPullRequestId + "/overview";
                    result.add(new TuleapLink("icon-git-branch", prUrl));
                }
            }
        return result;
    }
}
