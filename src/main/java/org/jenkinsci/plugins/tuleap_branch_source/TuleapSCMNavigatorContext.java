package org.jenkinsci.plugins.tuleap_branch_source;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.trait.SCMNavigatorContext;

public class TuleapSCMNavigatorContext extends SCMNavigatorContext<TuleapSCMNavigatorContext, TuleapSCMNavigatorRequest> {

    private boolean wantUserFork;

    @NonNull
    @Override
    public TuleapSCMNavigatorRequest newRequest(@NonNull SCMNavigator navigator, @NonNull SCMSourceObserver observer) {
        return new TuleapSCMNavigatorRequest(navigator, this, observer);
    }

    public TuleapSCMNavigatorContext wantUserFork(boolean include) {
        this.wantUserFork = wantUserFork || include;
        return this;
    }

    public boolean wantUserFork() {
        return wantUserFork;
    }
}
