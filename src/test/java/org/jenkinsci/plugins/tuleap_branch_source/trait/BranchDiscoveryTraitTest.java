package org.jenkinsci.plugins.tuleap_branch_source.trait;


import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import org.jenkinsci.plugins.tuleap_branch_source.TuleapSCMSourceContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import jenkins.scm.api.SCMHeadObserver;

public class BranchDiscoveryTraitTest {

	@ClassRule
	public static JenkinsRule j = new JenkinsRule();

	@Test
	public void given__discoverAll__when__appliedToContext__then__noFilter() throws Exception {
		TuleapSCMSourceContext ctx = new TuleapSCMSourceContext(null, SCMHeadObserver.none());
		assertThat(ctx.wantBranches(), is(false));
		BranchDiscoveryTrait instance = new BranchDiscoveryTrait();
		instance.decorateContext(ctx);
		assertThat(ctx.wantBranches(), is(true));
	}
}
