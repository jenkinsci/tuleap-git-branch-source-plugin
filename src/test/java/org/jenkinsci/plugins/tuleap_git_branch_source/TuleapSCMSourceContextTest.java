package org.jenkinsci.plugins.tuleap_git_branch_source;

import jenkins.scm.api.SCMHeadObserver;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TuleapSCMSourceContextTest {

    @Test
    public void testDefaultContext() {
        TuleapSCMSourceContext tuleapSourceContext = new TuleapSCMSourceContext(null, SCMHeadObserver.none());
        assertThat(tuleapSourceContext.wantBranches(), is(false));
    }
}
