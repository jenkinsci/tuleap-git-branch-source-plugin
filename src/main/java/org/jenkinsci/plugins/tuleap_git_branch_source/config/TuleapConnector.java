package org.jenkinsci.plugins.tuleap_git_branch_source.config;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


import org.jenkinsci.plugins.tuleap_git_branch_source.client.TuleapClientCommandConfigurer;
import org.jenkinsci.plugins.tuleap_git_branch_source.client.TuleapClientRawCmd;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;

import static com.cloudbees.plugins.credentials.CredentialsMatchers.filter;
import static com.cloudbees.plugins.credentials.CredentialsMatchers.withId;
import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.Util;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

public class TuleapConnector {

    public static ListBoxModel listScanCredentials(@CheckForNull @AncestorInPath Item context,
        @QueryParameter String apiUri, @QueryParameter String credentialsId, boolean includeEmpty) {
        if (context == null ?
                !Jenkins.getActiveInstance().hasPermission(Jenkins.ADMINISTER) :
                !context.hasPermission(Item.EXTENDED_READ)) {
            return new StandardListBoxModel().includeCurrentValue(credentialsId);
        }
        final StandardListBoxModel model = new StandardListBoxModel();
        if (includeEmpty) {
            model.includeEmptyValue();
        }
        return model.includeMatchingAs(
            context instanceof Queue.Task ? Tasks.getDefaultAuthenticationOf((Queue.Task) context) : ACL.SYSTEM,
            context, StandardUsernameCredentials.class, OFDomainRequirements(apiUri), allUsernamePasswordMatch());
    }

    public static FormValidation checkCredentials(@AncestorInPath Item item, String apiUri, String credentialsId) {
        if (item == null) {
            if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                return FormValidation.ok();
            }
        } else {
            if (!item.hasPermission(Item.EXTENDED_READ) && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                return FormValidation.ok();
            }
        }
        if (Util.fixEmpty(credentialsId) == null) {
            return FormValidation.error("Username Password credential is required");
        } else {
            StandardUsernamePasswordCredentials cred = CredentialsMatchers.firstOrNull(
                filter(lookupCredentials(StandardUsernamePasswordCredentials.class, Jenkins.get(), ACL.SYSTEM,
                    Collections.<DomainRequirement> emptyList()), withId(trimToEmpty(credentialsId))),
                CredentialsMatchers.allOf(withId(credentialsId), allUsernamePasswordMatch()));
            try {
                Boolean credentialsAreValid = TuleapClientCommandConfigurer.<Boolean>newInstance(
                    defaultIfEmpty(apiUri, TuleapConfiguration.get().getApiBaseUrl()))
                    .withCredentials(cred)
                    .withCommand(new TuleapClientRawCmd.IsCredentialsValid())
                    .configure()
                    .call();

                if (credentialsAreValid) {
                    return FormValidation.ok();
                } else {
                    return FormValidation.error("Failed to validate the provided credentials");
                }
            } catch (IOException | IllegalArgumentException e) {
                return FormValidation.error(e, "Failed to validate the provided credentials");
            }
        }
    }

    @CheckForNull
    public static StandardCredentials lookupScanCredentials(@CheckForNull Item context, @CheckForNull String apiUri,
        @CheckForNull String scanCredentialsId) {
        if (Util.fixEmpty(scanCredentialsId) == null) {
            return null;
        } else {
            return CredentialsMatchers
                .firstOrNull(
                    CredentialsProvider.lookupCredentials(StandardUsernameCredentials.class, context,
                        context instanceof Queue.Task ? Tasks.getDefaultAuthenticationOf((Queue.Task) context)
                            : ACL.SYSTEM,
                        OFDomainRequirements(apiUri)),
                    CredentialsMatchers.allOf(withId(scanCredentialsId),
                                              allUsernamePasswordMatch()));
        }
    }

    private static List<DomainRequirement> OFDomainRequirements(@CheckForNull String apiUri) {
        return URIRequirementBuilder.fromUri(defaultIfEmpty(apiUri, TuleapConfiguration.get().getApiBaseUrl())).build();
    }

    public static CredentialsMatcher allUsernamePasswordMatch() {
        return CredentialsMatchers.anyOf(CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class));
    }
}
