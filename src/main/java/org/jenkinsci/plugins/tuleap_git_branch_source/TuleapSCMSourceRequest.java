package org.jenkinsci.plugins.tuleap_git_branch_source;

import hudson.model.TaskListener;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceRequest;
import org.jetbrains.annotations.NotNull;

public class TuleapSCMSourceRequest extends SCMSourceRequest {
    protected TuleapSCMSourceRequest(
        @NotNull SCMSource source,
        @NotNull SCMSourceContext<?, ?> context,
        TaskListener listener
    ) {
        super(source, context, listener);
    }
}
