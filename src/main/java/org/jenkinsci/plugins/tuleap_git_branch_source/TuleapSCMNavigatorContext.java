package org.jenkinsci.plugins.tuleap_git_branch_source;

import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.trait.SCMNavigatorContext;

import javax.annotation.Nonnull;

public class TuleapSCMNavigatorContext extends SCMNavigatorContext<TuleapSCMNavigatorContext, TuleapSCMNavigatorRequest> {

    private boolean wantUserFork;

    @Nonnull
    @Override
    public TuleapSCMNavigatorRequest newRequest(@Nonnull SCMNavigator navigator, @Nonnull SCMSourceObserver observer) {
        return new TuleapSCMNavigatorRequest(navigator, this, observer);
    }

    public TuleapSCMNavigatorContext wantUserFork(boolean include) {
        this.wantUserFork = wantUserFork || include;
        return this;
    }
}
