package org.jenkinsci.plugins.tuleap_branch_source.config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


import org.jenkinsci.plugins.tuleap_branch_source.Messages;
import org.jenkinsci.plugins.tuleap_branch_source.client.TuleapClientCommandConfigurer;
import org.jenkinsci.plugins.tuleap_branch_source.client.TuleapClientRawCmd;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.jenkinsci.plugins.tuleap_branch_source.client.TuleapClient.DEFAULT_GIT_HTTPS_PATH;
import static org.jenkinsci.plugins.tuleap_branch_source.client.TuleapClient.DEFAULT_TULEAP_API_PATH;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;

@Extension
public class TuleapConfiguration extends GlobalConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(TuleapConfiguration.class);
    private String domainUrl;

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


    public String getDomainUrl() {
        return domainUrl;
    }

    @DataBoundSetter
    public void setDomainUrl(String domainUrl) {
        this.domainUrl = domainUrl;
    }

    public String getApiBaseUrl() {
        return domainUrl + DEFAULT_TULEAP_API_PATH;
    }

    public String getGitBaseUrl() {
        return domainUrl + DEFAULT_GIT_HTTPS_PATH;
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
    public FormValidation doVerifyUrls(@QueryParameter String domainUrl) throws
        IOException {
        setDomainUrl(defaultString(domainUrl));
        final TuleapClientRawCmd.Command<Boolean> isUrlValidRawCmd = new TuleapClientRawCmd.IsTuleapServerUrlValid();
        TuleapClientRawCmd.Command<Boolean> configuredCmd = TuleapClientCommandConfigurer.<Boolean> newInstance(getApiBaseUrl())
            .withCommand(isUrlValidRawCmd)
            .configure();
        try {
            if (configuredCmd.call()) {
                return FormValidation.ok("Connexion established with these Urls");
            } else {
                return FormValidation.error("Failed to validate the account");
            }
        } catch (IOException e) {
            return FormValidation.error(e, "Failed to validate the account");
        }
    }

    @SuppressWarnings("unused")
    public FormValidation doCheckDomainUrl(@QueryParameter String domainUrl) {
        setDomainUrl(defaultString(domainUrl));
        return validateUrls(domainUrl);
    }

    private FormValidation validateUrls(final String url) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            return FormValidation.error("Malformed OrangeForge url (%s)", e.getMessage());
        }

        if (isEmpty(url)) {
            return FormValidation.error("Url is required and should be valid");
        }

        return FormValidation.ok();
    }
}
