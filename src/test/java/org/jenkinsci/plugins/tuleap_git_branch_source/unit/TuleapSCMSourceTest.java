package org.jenkinsci.plugins.tuleap_git_branch_source.unit;

import hudson.model.TaskListener;
import hudson.util.LogTaskListener;
import io.jenkins.plugins.tuleap_api.client.GitHead;
import io.jenkins.plugins.tuleap_api.client.GitPullRequest;
import io.jenkins.plugins.tuleap_api.client.GitRepositoryReference;
import io.jenkins.plugins.tuleap_api.deprecated_client.api.TuleapGitRepository;
import io.jenkins.plugins.tuleap_api.deprecated_client.api.TuleapProject;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.SCMRevision;
import org.jenkinsci.plugins.tuleap_git_branch_source.*;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;


public class TuleapSCMSourceTest {

    @Test
    public void itReturnsTheGivenRevisionIfThisIsNotATuleapSCMRevision() throws IOException, InterruptedException {
        SCMHead head = new SCMHead("dummy head");
        SCMRevision revision = new SCMRevision(head) {
            @Override
            public boolean equals(Object obj) {
                return false;
            }

            @Override
            public int hashCode() {
                return 0;
            }
        };

        TaskListener listener = new LogTaskListener(Logger.getLogger(getClass().getName()), Level.FINE);

        TuleapProject project = new TuleapProject();
        TuleapGitRepository repository = new TuleapGitRepository();
        TuleapSCMSource tlpSCMSource = new TuleapSCMSource(project, repository);

        SCMRevision resultRevision = tlpSCMSource.getTrustedRevision(revision, listener);

        assertEquals(revision.getHead().getName(), resultRevision.getHead().getName());
        assertEquals(revision.hashCode(), resultRevision.hashCode());
    }

    @Test
    public void itReturnsTheTargetRevisionIfTheSourceRevisionIsNotFromATrustedSource() throws IOException, InterruptedException {
        GitPullRequest pullRequest = new GitPullRequest() {
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
                return "source-branch";
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

        SCMHeadOrigin originHead = SCMHeadOrigin.DEFAULT;
        TuleapBranchSCMHead targetHead = new TuleapBranchSCMHead("my-c63-tlp-branch");
        TuleapPullRequestSCMHead head = new TuleapPullRequestSCMHead(pullRequest, originHead, targetHead,10,10, "refs/tlpr/4");

        TuleapBranchSCMRevision target = new TuleapBranchSCMRevision(head.getTarget(), "h4sH_t4rG3t");
        TuleapBranchSCMRevision origin = new TuleapBranchSCMRevision(new TuleapBranchSCMHead(head.getOriginName()), "h4sH_or1g1n");
        TuleapPullRequestRevision revision = new TuleapPullRequestRevision(head, target, origin);

        TaskListener listener = new LogTaskListener(Logger.getLogger(getClass().getName()), Level.FINE);

        TuleapProject project = new TuleapProject();
        TuleapGitRepository repository = new TuleapGitRepository();
        TuleapSCMSource tlpSCMSource = new TuleapSCMSource(project, repository);

        SCMRevision resultRevision = tlpSCMSource.getTrustedRevision(revision, listener);

        assertEquals(revision.getTarget().getHead().getName(), resultRevision.getHead().getName());
    }
}
