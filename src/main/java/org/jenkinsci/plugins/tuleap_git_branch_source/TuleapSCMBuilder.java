package org.jenkinsci.plugins.tuleap_git_branch_source;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.plugins.git.GitSCM;
import jenkins.plugins.git.GitSCMBuilder;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import org.jetbrains.annotations.NotNull;

public class TuleapSCMBuilder extends GitSCMBuilder<TuleapSCMBuilder> {

    private String repositoryBaseUrl;

    public TuleapSCMBuilder(@NotNull SCMHead head, SCMRevision revision, @NotNull String remote, String credentialsId, String repositoryBaseUrl) {
        super(head, revision, remote, credentialsId);
        withoutRefSpecs();
        if (head instanceof TuleapPullRequestSCMHead) {
            TuleapPullRequestSCMHead tuleapPullRequestSCMHead = (TuleapPullRequestSCMHead) head;
            withRefSpec("+" + tuleapPullRequestSCMHead.getHeadReference() + ":refs/remotes/@{remote}/TLP-PR-" + tuleapPullRequestSCMHead.getId());
        } else {
            withRefSpec("+refs/heads/" + head.getName() + ":refs/remotes/@{remote}/" + head.getName());
        }
        this.repositoryBaseUrl = repositoryBaseUrl;
        withBrowser(new TuleapBrowser(repositoryBaseUrl));
    }

    @NonNull
    @Override
    public GitSCM build() {
        final SCMHead head = head();
        final SCMRevision revision = revision();
        try {
            if (head instanceof TuleapPullRequestSCMHead && revision instanceof TuleapPullRequestRevision) {
                withRevision(((TuleapPullRequestRevision) revision).getOrigin());
            }
            return super.build();
        } finally {
            withHead(head);
            withRevision(revision);
            withBrowser(new TuleapBrowser(this.repositoryBaseUrl));
        }
    }
}
