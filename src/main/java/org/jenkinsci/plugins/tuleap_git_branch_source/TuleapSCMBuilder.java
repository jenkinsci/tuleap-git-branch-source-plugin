package org.jenkinsci.plugins.tuleap_git_branch_source;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.plugins.git.GitSCM;
import jenkins.plugins.git.GitSCMBuilder;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import org.jetbrains.annotations.NotNull;

public class TuleapSCMBuilder extends GitSCMBuilder<TuleapSCMBuilder> {
    public TuleapSCMBuilder(@NotNull SCMHead head, SCMRevision revision, @NotNull String remote, String credentialsId, String repositoryBaseUrl) {
        super(head, revision, remote, credentialsId);
        withoutRefSpecs();
        if (head instanceof TuleapPullRequestSCMHead) {
            TuleapPullRequestSCMHead tuleapPullRequestSCMHead = (TuleapPullRequestSCMHead) head;
            withRefSpec("+refs/tlpr/" + tuleapPullRequestSCMHead.getId() + "/head:refs/remotes/@{remote}/" + tuleapPullRequestSCMHead.getName());
        } else {
            withRefSpec("+refs/heads/" + head.getName() + ":refs/remotes/@{remote}/" + head.getName());
        }
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
        }
    }
}
