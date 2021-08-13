package org.jenkinsci.plugins.tuleap_git_branch_source;

import hudson.model.TaskListener;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMSourceCriteria;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TuleapSCMSourceContextTest {

    @Test
    public void testDefaultContext() {
        TuleapSCMSourceContext tuleapSourceContext = new TuleapSCMSourceContext(null, SCMHeadObserver.none());
        assertFalse(tuleapSourceContext.wantBranches());
    }

    @Test
    public void testWeDoesNotWantToRetrieveOriginPullRequest(){
        SCMSourceCriteria criteria = new SCMSourceCriteria() {
            @Override
            public boolean isHead(@NotNull Probe probe, @NotNull TaskListener listener) throws IOException {
                return false;
            }
        };
        TuleapSCMSourceContext context = new TuleapSCMSourceContext(criteria, SCMHeadObserver.none());

        context.wantOriginPullRequests(false);
        assertFalse(context.wantOriginPullRequests());
    }
    @Test
    public void testWeWantToRetrieveOriginPullRequest(){
        SCMSourceCriteria criteria = new SCMSourceCriteria() {
            @Override
            public boolean isHead(@NotNull Probe probe, @NotNull TaskListener listener) throws IOException {
                return false;
            }
        };
        TuleapSCMSourceContext context = new TuleapSCMSourceContext(criteria, SCMHeadObserver.none());

        context.wantOriginPullRequests(true);
        assertTrue(context.wantOriginPullRequests());
    }
}
