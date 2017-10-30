package com.francetelecom.faas.jenkinsfaasbranchsource.config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;


import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.francetelecom.faas.jenkinsfaasbranchsource.Messages;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.TuleapClientCommandConfigurer;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.TuleapClientRawCmd;

import static com.cloudbees.plugins.credentials.CredentialsMatchers.filter;
import static com.cloudbees.plugins.credentials.CredentialsMatchers.withId;
import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;
import static com.francetelecom.faas.jenkinsfaasbranchsource.config.TuleapConnector.allUsernamePasswordMatch;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

@Extension
public class TuleapConfiguration extends GlobalConfiguration {

    public static final String ORANGEFORGE_URL = "https://www.forge.orange-labs.fr";
    public static final String ORANGEFORGE_API_URL = ORANGEFORGE_URL + "/api";
    public static final String ORANGEFORGE_GIT_HTTPS_URL = ORANGEFORGE_URL + "/plugins/git/";
    private static final Logger LOGGER = LoggerFactory.getLogger(TuleapConfiguration.class);
    private String apiBaseUrl = ORANGEFORGE_API_URL;
    /**
     * Git URL as configured in /etc/tuleap/plugins/git/etc/config.inc
     * https://tuleap.net/pipermail/tuleap-devel/2015-December/004425.html
     * http://tuleap-documentation.readthedocs.io/en/latest/installation-guide/advanced-configuration.html#tuleap-configuration
     */
    private String gitBaseUrl = ORANGEFORGE_GIT_HTTPS_URL;

    public TuleapConfiguration() throws IOException {
        load();
    }

    public static TuleapConfiguration get() {
        return GlobalConfiguration.all().get(TuleapConfiguration.class);
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        return true;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    @DataBoundSetter
    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getGitBaseUrl() {
        return gitBaseUrl;
    }

    @DataBoundSetter
    public void setGitBaseUrl(String gitBaseUrl) {
        this.gitBaseUrl = gitBaseUrl;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public String getDisplayName() {
        return Messages.OFConfiguration_displayName();
    }

    @SuppressWarnings("unused")
    public ListBoxModel doFillCredentialsIdItems(@QueryParameter String apiUrl, @QueryParameter String credentialsId) {
        return TuleapConnector.listScanCredentials(null, apiUrl, credentialsId);
    }

    @SuppressWarnings("unused")
    public FormValidation doVerifyCredentials(@QueryParameter String apiBaseUrl, @QueryParameter String gitBaseUrl,
        @QueryParameter String credentialsId) throws IOException {

        if (Util.fixEmpty(credentialsId) == null) {
            return FormValidation.error("Username Password credential is required");
        } else {
            StandardUsernamePasswordCredentials cred = CredentialsMatchers
                .firstOrNull(
                    filter(lookupCredentials(StandardUsernamePasswordCredentials.class, Jenkins.getInstance(), ACL.SYSTEM,
                        Collections.<DomainRequirement> emptyList()), withId(trimToEmpty(credentialsId))),
                    CredentialsMatchers.allOf(CredentialsMatchers.withId(credentialsId), allUsernamePasswordMatch()));

            // TODO isValidTuleapUrl no need authentication go anonynous
            final TuleapClientRawCmd.Command<Boolean> isCredentialValidRawCmd = new TuleapClientRawCmd().new IsTuleapServerUrl();
            TuleapClientRawCmd.Command<Boolean> configuredCmd = TuleapClientCommandConfigurer.<Boolean> newInstance(apiBaseUrl)
                .withCredentials(cred)
                .withCommand(isCredentialValidRawCmd)
                .configure();
            try {
                if (configuredCmd.call()) {
                    return FormValidation.ok("Credentials verified for user %s", cred.getUsername());
                } else {
                    return FormValidation.error("Failed to validate the account");
                }
            } catch (IOException e) {
                return FormValidation.error(e, "Failed to validate the account");
            }
        }
    }

    @SuppressWarnings("unused")
    public FormValidation doCheckApiBaseUrl(@QueryParameter String apiBaseUrl) {
        return validateUrls(apiBaseUrl, ORANGEFORGE_API_URL);
    }

    @SuppressWarnings("unused")
    public FormValidation doCheckGitBaseUrl(@QueryParameter String gitBaseUrl) {
        return validateUrls(gitBaseUrl, ORANGEFORGE_GIT_HTTPS_URL);
    }

    private FormValidation validateUrls(final String url, final String pattern) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            return FormValidation.error("Malformed OrangeForge url (%s)", e.getMessage());
        }

        if (pattern.equals(url)) {
            return FormValidation.ok();
        }

        return FormValidation.warning("OrangeForge Urls are required and should be valid");
    }
}
