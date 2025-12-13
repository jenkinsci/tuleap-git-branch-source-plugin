package org.jenkinsci.plugins.tuleap_git_branch_source.stubs;

import hudson.model.TaskListener;
import jenkins.scm.api.SCMSourceCriteria;
import org.jetbrains.annotations.NotNull;

public class SCMSourceCriteriaDefaultStub implements SCMSourceCriteria {
    @Override
    public boolean isHead(@NotNull Probe probe, @NotNull TaskListener listener) {
        return false;
    }
}
