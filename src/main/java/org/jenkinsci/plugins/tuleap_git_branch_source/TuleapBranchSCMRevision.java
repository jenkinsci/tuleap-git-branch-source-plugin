package org.jenkinsci.plugins.tuleap_git_branch_source;

import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMHead;

public class TuleapBranchSCMRevision extends AbstractGitSCMSource.SCMRevisionImpl {
    public TuleapBranchSCMRevision(SCMHead head, String hash) {
        super(head, hash);
    }
}
