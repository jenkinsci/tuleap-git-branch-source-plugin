package org.jenkinsci.plugins.tuleap_git_branch_source.stubs;

import hudson.model.TaskListener;
import jenkins.scm.api.SCMSourceCriteria;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SCMSourceCriteriaDefaultStub implements SCMSourceCriteria {
    @Override
    public boolean isHead(@NotNull Probe probe, @NotNull TaskListener listener) throws IOException {
        return false;
    }
}
