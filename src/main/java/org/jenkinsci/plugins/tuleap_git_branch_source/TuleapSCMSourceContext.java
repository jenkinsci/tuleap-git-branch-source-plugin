package org.jenkinsci.plugins.tuleap_git_branch_source;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.trait.SCMSourceContext;

public class TuleapSCMSourceContext extends SCMSourceContext<TuleapSCMSourceContext, TuleapSCMSourceRequest> {

    public TuleapSCMSourceContext(@CheckForNull SCMSourceCriteria criteria, @NonNull SCMHeadObserver observer) {
        super(criteria, observer);
    }

    @NonNull
    @Override
    public TuleapSCMSourceRequest newRequest(@NonNull SCMSource scmSource, @CheckForNull TaskListener taskListener) {
        return new TuleapSCMSourceRequest(scmSource, this, taskListener);
    }
}
