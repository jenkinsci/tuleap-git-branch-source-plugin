package org.jenkinsci.plugins.tuleap_git_branch_source.unit;

import hudson.model.TaskListener;
import hudson.util.LogTaskListener;
import io.jenkins.plugins.tuleap_api.deprecated_client.api.TuleapGitRepository;
import io.jenkins.plugins.tuleap_api.deprecated_client.api.TuleapProject;
import jenkins.scm.api.SCMHeadObserver;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapSCMSource;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapSCMSourceContext;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapSCMSourceRequest;
import org.jenkinsci.plugins.tuleap_git_branch_source.stubs.SCMSourceCriteriaDefaultStub;
import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static junit.framework.TestCase.*;

public class TuleapSCMRequestTest {

    @Test
    public void testWeWantToRetrievePullRequestWhenWeTheWantForkTraitIsEnabled() {

        TuleapSCMSource source = new TuleapSCMSource(new TuleapProject(), new TuleapGitRepository());

        SCMSourceCriteriaDefaultStub criteriaDefaultStub = new SCMSourceCriteriaDefaultStub();
        TuleapSCMSourceContext context = new TuleapSCMSourceContext(criteriaDefaultStub, SCMHeadObserver.none());
        context.wantForkPullRequests(true);
        context.wantOriginPullRequests(false);

        TaskListener listener = new LogTaskListener(Logger.getLogger(getClass().getName()), Level.FINE);

        TuleapSCMSourceRequest request = context.newRequest(source,listener);

        assertTrue(request.isRetrievePullRequests());
    }

    @Test
    public void testWeWantToRetrievePullRequestWhenWeTheWantOriginTraitIsEnabled() {

        TuleapSCMSource source = new TuleapSCMSource(new TuleapProject(), new TuleapGitRepository());

        SCMSourceCriteriaDefaultStub criteriaDefaultStub = new SCMSourceCriteriaDefaultStub();
        TuleapSCMSourceContext context = new TuleapSCMSourceContext(criteriaDefaultStub, SCMHeadObserver.none());
        context.wantForkPullRequests(false);
        context.wantOriginPullRequests(true);

        TaskListener listener = new LogTaskListener(Logger.getLogger(getClass().getName()), Level.FINE);

        TuleapSCMSourceRequest request = context.newRequest(source,listener);

        assertTrue(request.isRetrievePullRequests());
    }

    @Test
    public void testWeDoNotWantToRetrievePullRequestWhenAnyPullRequestTraitIsEnabled() {

        TuleapSCMSource source = new TuleapSCMSource(new TuleapProject(), new TuleapGitRepository());

        SCMSourceCriteriaDefaultStub criteriaDefaultStub = new SCMSourceCriteriaDefaultStub();
        TuleapSCMSourceContext context = new TuleapSCMSourceContext(criteriaDefaultStub, SCMHeadObserver.none());
        context.wantForkPullRequests(false);
        context.wantOriginPullRequests(false);

        TaskListener listener = new LogTaskListener(Logger.getLogger(getClass().getName()), Level.FINE);

        TuleapSCMSourceRequest request = context.newRequest(source,listener);

        assertFalse(request.isRetrievePullRequests());
    }
}
