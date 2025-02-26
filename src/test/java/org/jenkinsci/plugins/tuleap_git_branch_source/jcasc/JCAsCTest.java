package org.jenkinsci.plugins.tuleap_git_branch_source.jcasc;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.security.ACL;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.tuleap_credentials.TuleapAccessToken;
import io.jenkins.plugins.tuleap_server_configuration.TuleapConfiguration;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JCAsCTest {

    @ClassRule
    @ConfiguredWithCode("jenkins.yml")
    public static JenkinsConfiguredWithCodeRule jenkins = new JenkinsConfiguredWithCodeRule();

    @Test
    public void itShouldImportTheTuleapAccessToken() {
        final List<TuleapAccessToken> tokens = CredentialsProvider.lookupCredentialsInItemGroup(
            TuleapAccessToken.class,
            jenkins.jenkins,
            ACL.SYSTEM2,
            Collections.emptyList()
        );

        assertThat(tokens, hasSize(1));
        assertThat(tokens.get(0).getToken().getPlainText(), is("tlp-k1-24.2dd47d2e9280a430f4eb862cfbc85e90e8b286818dcbe85bd60d8feb7162580a"));
    }

    @Test
    public void itShouldImportTheTuleapServerConfiguration() {
        final TuleapConfiguration configuration = TuleapConfiguration.get();

        assertThat(configuration.getDomainUrl(), is("https://tuleap.example.net"));
    }
}
