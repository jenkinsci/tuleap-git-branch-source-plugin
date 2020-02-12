package org.jenkinsci.plugins.tuleap_credentials;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsScope;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;
import org.kohsuke.stapler.DataBoundConstructor;

public class TuleapAccessTokenImplTest {
    @ClassRule
    public static JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void configRoundtrip() throws Exception {
        TuleapAccessTokenImpl expectedToken = new TuleapAccessTokenImpl(
            CredentialsScope.GLOBAL,
            "magic-id",
            "configRoundtrip",
            "tlp-k1-6.d94b1a79cc0a2f0b9ffb667749eb5567a341fbf08e1c260c2f4b8c5b130bbdb0");
        CredentialsBuilder builder = new CredentialsBuilder(expectedToken);
        jenkinsRule.configRoundtrip(builder);
        jenkinsRule.assertEqualDataBoundBeans(expectedToken, builder.credentials);
    }

    public static class CredentialsBuilder extends Builder {

        public final Credentials credentials;

        @DataBoundConstructor
        public CredentialsBuilder(Credentials credentials) {
            this.credentials = credentials;
        }

        @TestExtension
        public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

            @Override
            public String getDisplayName() {
                return "CredentialsBuilder";
            }

            @Override
            public boolean isApplicable(Class<? extends AbstractProject> jobType) {
                return true;
            }

        }

    }
}
