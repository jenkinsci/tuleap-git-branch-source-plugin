package org.jenkinsci.plugins.tuleap_git_branch_source;

import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.trait.SCMNavigatorContext;
import jenkins.scm.api.trait.SCMNavigatorRequest;

import javax.annotation.Nonnull;

public class TuleapSCMNavigatorRequest extends SCMNavigatorRequest {

    protected TuleapSCMNavigatorRequest(@Nonnull SCMNavigator source, @Nonnull SCMNavigatorContext<?, ?> context,
                                        @Nonnull SCMSourceObserver observer) {
        super(source, context, observer);
    }
}
