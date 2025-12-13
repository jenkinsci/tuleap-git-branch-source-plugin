package org.jenkinsci.plugins.tuleap_git_branch_source;

import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMSourceCriteria;
import org.junit.Test;

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
        SCMSourceCriteria criteria = (probe, listener) -> false;
        TuleapSCMSourceContext context = new TuleapSCMSourceContext(criteria, SCMHeadObserver.none());

        context.wantOriginPullRequests(false);
        assertFalse(context.wantOriginPullRequests());
    }
    @Test
    public void testWeWantToRetrieveOriginPullRequest(){
        SCMSourceCriteria criteria = (probe, listener) -> false;
        TuleapSCMSourceContext context = new TuleapSCMSourceContext(criteria, SCMHeadObserver.none());

        context.wantOriginPullRequests(true);
        assertTrue(context.wantOriginPullRequests());
    }
}
