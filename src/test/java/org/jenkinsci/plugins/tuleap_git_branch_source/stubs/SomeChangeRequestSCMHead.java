package org.jenkinsci.plugins.tuleap_git_branch_source.stubs;

import jenkins.scm.api.SCMHead;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.api.mixin.ChangeRequestSCMHead2;
import org.jetbrains.annotations.NotNull;

public class SomeChangeRequestSCMHead extends SCMHead implements ChangeRequestSCMHead2 {

    public SomeChangeRequestSCMHead(@NotNull String name) {
        super(name);
    }

    @NotNull
    @Override
    public ChangeRequestCheckoutStrategy getCheckoutStrategy() {
        return null;
    }

    @NotNull
    @Override
    public String getOriginName() {
        return null;
    }

    @NotNull
    @Override
    public String getId() {
        return null;
    }

    @NotNull
    @Override
    public SCMHead getTarget() {
        return new SCMHead(getName());
    }
}
