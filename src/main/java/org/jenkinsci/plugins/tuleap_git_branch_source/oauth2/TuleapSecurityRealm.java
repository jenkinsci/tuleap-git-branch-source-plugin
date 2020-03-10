package org.jenkinsci.plugins.tuleap_git_branch_source.oauth2;

import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.security.SecurityRealm;
import hudson.util.Secret;
import org.jenkinsci.plugins.tuleap_git_branch_source.config.TuleapConfiguration;
import org.kohsuke.stapler.DataBoundConstructor;

public class TuleapSecurityRealm extends SecurityRealm {

    private String tuleapUri;
    private String clientId;
    private Secret clientSecret;

    @DataBoundConstructor
    public TuleapSecurityRealm(String tuleapUri, String clientId, String clientSecret) {
        this.tuleapUri = Util.fixEmptyAndTrim(tuleapUri);
        this.clientId = Util.fixEmptyAndTrim(clientId);
        this.setClientSecret(Util.fixEmptyAndTrim(clientSecret));
    }

    public String getTuleapUri() {
        return tuleapUri;
    }

    public String getClientId() {
        return clientId;
    }

    public Secret getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String secretString) {
        this.clientSecret = Secret.fromString(secretString);
    }

    @Override
    public SecurityComponents createSecurityComponents() {
        return null;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<SecurityRealm> {
        @Override
        public String getDisplayName() {
            return "Tuleap Authentication";
        }

        public String getDefaultTuleapDomainUrl() {
            return TuleapConfiguration.get().getDomainUrl();
        }
    }
}
