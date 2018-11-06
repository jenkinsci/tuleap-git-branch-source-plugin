package org.jenkinsci.plugins.tuleap_git_branch_source.config;

import java.io.IOException;


import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.jenkinsci.plugins.tuleap_git_branch_source.client.TuleapClient.DEFAULT_TULEAP_DOMAIN_URL;

import hudson.util.FormValidation;

public class TuleapConfigurationTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();
    private TuleapConfiguration instance;
    private static String VALID_TULEAP_URL = "https://tuleap.net";

    @Before
    public void setup() {
        instance = TuleapConfiguration.get();
    }
    @Test
    public void testConnexion_malformed_url() throws IOException {
        final FormValidation formValidation = instance.doCheckDomainUrl("url.sucks.test");
        assertThat(formValidation.kind, is(FormValidation.Kind.ERROR));
        assertThat(formValidation.getMessage(), anything("Malformed url"));
    }

    @Test
    public void testConnexion_fake_url() throws IOException {
        final FormValidation formValidation = instance.doCheckDomainUrl("http://aaa.bb.test");
        assertThat(formValidation.kind, is(FormValidation.Kind.ERROR));
        assertThat(formValidation.getMessage(), anything("Failed to validate url"));
    }

    @Test
    public void testConnexion_ok_url() throws IOException {
        final FormValidation formValidation = instance.doCheckDomainUrl(VALID_TULEAP_URL);
        assertThat(formValidation.kind, is(FormValidation.Kind.OK));
    }
}
