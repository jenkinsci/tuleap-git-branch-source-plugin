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
import com.francetelecom.faas.jenkinsfaasbranchsource.OFClient;

import static com.cloudbees.plugins.credentials.CredentialsMatchers.filter;
import static com.cloudbees.plugins.credentials.CredentialsMatchers.withId;
import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;
import static com.francetelecom.faas.jenkinsfaasbranchsource.config.OFConnector.allUsernamePasswordMatch;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.Extension;
import hudson.Util;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

@Extension
public class OFConfiguration extends GlobalConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(OFConfiguration.class);

	public static final String ORANGEFORGE_API_URL= "https://www.forge.orange-labs.fr/api";
	public static final String ORANGEFORGE_GIT_HTTPS_URL= "https://www.forge.orange-labs.fr/plugins/git/";

	public static OFConfiguration get() {
		return GlobalConfiguration.all().get(OFConfiguration.class);
	}

	@CheckForNull
	private String name;
	private String gitBaseUrl = ORANGEFORGE_GIT_HTTPS_URL;
	private String apiBaseUrl = ORANGEFORGE_API_URL;

	public OFConfiguration() throws IOException {
		load();
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

	public String getName() {
		return name;
	}

	@DataBoundSetter
	public void setName(String name) {
		this.name = name;
	}

	@SuppressWarnings("unused")
	public ListBoxModel doFillCredentialsIdItems(@QueryParameter String apiUrl,
												 @QueryParameter String credentialsId) {
		return OFConnector.listScanCredentials(null, apiUrl, credentialsId);
	}

	@SuppressWarnings("unused")
	public FormValidation doVerifyCredentials(
			@QueryParameter String apiUrl, @QueryParameter String gitUrl,
			@QueryParameter String credentialsId) throws IOException {

		if (Util.fixEmpty(credentialsId) == null) {
			return FormValidation.error("Username Password credential is required");
		} else {
			StandardUsernamePasswordCredentials cred = CredentialsMatchers.firstOrNull(filter(
					lookupCredentials(StandardUsernamePasswordCredentials.class,
									  Jenkins.getInstance(), ACL.SYSTEM,
									  Collections.<DomainRequirement>emptyList()),
					withId(trimToEmpty(credentialsId))
			), CredentialsMatchers.allOf(CredentialsMatchers.withId(credentialsId), allUsernamePasswordMatch()));

			OFClient client = new OFClient(cred, apiUrl, gitUrl);

			try {
				if (client.isCredentialValid()) {
					return FormValidation.ok("Credentials verified ");
				} else {
					return FormValidation.error("Failed to validate the account");
				}
			} catch (IOException e) {
				return FormValidation.error(e, "Failed to validate the account");
			}
		}
	}

	@SuppressWarnings("unused")
	public FormValidation doCheckApiUrl(@QueryParameter String value) {
		try {
			new URL(value);
		} catch (MalformedURLException e) {
			return FormValidation.error("Malformed GitHub url (%s)", e.getMessage());
		}

		if (ORANGEFORGE_API_URL.equals(value)) {
			return FormValidation.ok();
		}

		if (value.endsWith("/api/v3") || value.endsWith("/api/v3/")) {
			return FormValidation.ok();
		}

		return FormValidation.warning("GitHub Enterprise API URL ends with \"/api/v3\"");
	}
}
