package org.jenkinsci.plugins.tuleap_git_branch_source.stubs;

import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.mixin.ChangeRequestSCMRevision;
import org.jetbrains.annotations.NotNull;

public class SomeChangeRequestRevision extends ChangeRequestSCMRevision<SomeChangeRequestSCMHead> {
    public SomeChangeRequestRevision(@NotNull SomeChangeRequestSCMHead head, @NotNull SCMRevision target) {
        super(head, target);
    }

    @Override
    public boolean equivalent(ChangeRequestSCMRevision<?> revision) {
        return true;
    }

    @Override
    protected int _hashCode() {
        return 0;
    }
}
