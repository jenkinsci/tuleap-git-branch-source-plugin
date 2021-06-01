package org.jenkinsci.plugins.tuleap_git_branch_source.trait;

import jenkins.scm.api.SCMHeadObserver;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapSCMSourceContext;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TuleapBranchDiscoveryTraitTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    @Test
    public void testContextWantsBranchesIfTraitIsLoaded() {
        TuleapSCMSourceContext tuleapSourceContext = new TuleapSCMSourceContext(null, SCMHeadObserver.none());
        TuleapBranchDiscoveryTrait branchDiscoveryTrait = new TuleapBranchDiscoveryTrait();

        tuleapSourceContext.withTrait(branchDiscoveryTrait);

        assertThat(tuleapSourceContext.wantBranches(), is(true));
    }
}
