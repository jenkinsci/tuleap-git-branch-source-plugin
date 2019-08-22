package org.jenkinsci.plugins.tuleap_git_branch_source.config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


import hudson.Util;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.tuleap_git_branch_source.Messages;
import org.jenkinsci.plugins.tuleap_git_branch_source.client.TuleapClientCommandConfigurer;
import org.jenkinsci.plugins.tuleap_git_branch_source.client.TuleapClientRawCmd;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.verb.POST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.jenkinsci.plugins.tuleap_git_branch_source.client.TuleapClient.DEFAULT_GIT_HTTPS_PATH;
import static org.jenkinsci.plugins.tuleap_git_branch_source.client.TuleapClient.DEFAULT_TULEAP_API_PATH;
import static org.jenkinsci.plugins.tuleap_git_branch_source.client.TuleapClient.DEFAULT_TULEAP_DOMAIN_URL;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;

import javax.annotation.Nonnull;

@Extension
public class TuleapConfiguration extends GlobalConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(TuleapConfiguration.class);
    private String domainUrl;

    public TuleapConfiguration() {
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
        return defaultIfEmpty(domainUrl, DEFAULT_TULEAP_DOMAIN_URL);
    }

    @DataBoundSetter
    public void setDomainUrl(String domainUrl) {
        this.domainUrl = domainUrl;
        if (this.domainUrl.charAt(domainUrl.length() - 1) == '/') {
            this.domainUrl = domainUrl.substring(0, domainUrl.length() - 1);
        }
        save();
    }

    public String getApiBaseUrl() {
        return getDomainUrl() + DEFAULT_TULEAP_API_PATH;
    }

    public String getGitBaseUrl() {
        return getDomainUrl() + DEFAULT_GIT_HTTPS_PATH;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String getDisplayName() {
        return Messages.Configuration_displayName();
    }

    @POST
    @SuppressWarnings("unused")
    public FormValidation doCheckDomainUrl(@QueryParameter String domainUrl)
    {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        final FormValidation validation = checkDomainUrl(domainUrl);
        if (!FormValidation.Kind.OK.equals(validation.kind)) {
            return validation;
        }
        setDomainUrl(domainUrl);
        try {
            boolean serverUrlIsValid = TuleapClientCommandConfigurer.<Boolean>newInstance(getApiBaseUrl())
                .withCommand(new TuleapClientRawCmd.IsTuleapServerUrlValid())
                .configure()
                .call();

            if (serverUrlIsValid) {
                return FormValidation.ok("Connexion established with these Urls");
            } else {
                return FormValidation.error("Failed to validate the account");
            }
        } catch (IOException e) {
            return FormValidation.error(e, "Failed to validate url");
        }
    }

    @SuppressWarnings("unused")
    public FormValidation checkDomainUrl(@QueryParameter String domainUrl) {
        return validateUrls(domainUrl);
    }

    private FormValidation validateUrls(final String url) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            return FormValidation.error("Malformed url (%s)", e.getMessage());
        }

        if (Util.fixEmptyAndTrim(url) == null) {
            return FormValidation.error("Url is required and should be valid");
        }

        return FormValidation.ok();
    }
}
