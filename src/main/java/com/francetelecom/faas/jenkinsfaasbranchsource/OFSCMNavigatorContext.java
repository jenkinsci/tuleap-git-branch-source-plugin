package com.francetelecom.faas.jenkinsfaasbranchsource;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.trait.SCMNavigatorContext;

public class OFSCMNavigatorContext extends SCMNavigatorContext<OFSCMNavigatorContext, OFSCMNavigatorRequest> {

    private boolean wantUserFork;

    @NonNull
    @Override
    public OFSCMNavigatorRequest newRequest(@NonNull SCMNavigator navigator, @NonNull SCMSourceObserver observer) {
        return new OFSCMNavigatorRequest(navigator, this, observer);
    }

    public OFSCMNavigatorContext wantUserFork(boolean include) {
        this.wantUserFork = wantUserFork || include;
        return this;
    }

    public boolean wantUserFork() {
        return wantUserFork;
    }
}
