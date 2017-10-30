package com.francetelecom.faas.jenkinsfaasbranchsource.config;

import java.util.List;


import org.apache.commons.lang.StringUtils;
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

import static com.francetelecom.faas.jenkinsfaasbranchsource.config.TuleapConfiguration.ORANGEFORGE_API_URL;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.Util;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

/**
 * Created by qsqf2513 on 10/16/17.
 */
public class TuleapConnector {

    public static ListBoxModel listScanCredentials(@CheckForNull @AncestorInPath Item context,
        @QueryParameter String apiUri, @QueryParameter String credentialsId) {
        if (context == null ?
                !Jenkins.getActiveInstance().hasPermission(Jenkins.ADMINISTER) :
                !context.hasPermission(Item.EXTENDED_READ)) {
            return new StandardListBoxModel().includeCurrentValue(credentialsId);
        }
        return new StandardListBoxModel().includeEmptyValue().includeMatchingAs(
            context instanceof Queue.Task ? Tasks.getDefaultAuthenticationOf((Queue.Task) context) : ACL.SYSTEM,
            context, StandardUsernameCredentials.class, OFDomainRequirements(apiUri), allUsernamePasswordMatch());
    }

    public static FormValidation checkCredentials(@AncestorInPath Item item, String apiUri) {
        if (item == null) {
            if (!Jenkins.getActiveInstance().hasPermission(Jenkins.ADMINISTER)) {
                return FormValidation.ok();
            }
        } else {
            if (!item.hasPermission(Item.EXTENDED_READ) && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                return FormValidation.ok();
            }
        }
        // TODO check credential exists, ask orangeforge if credentials are valid credentials and then ok else
        // invalid
        if (CredentialsProvider.listCredentials(StandardUsernamePasswordCredentials.class, item,
            item instanceof Queue.Task ? Tasks.getAuthenticationOf((Queue.Task) item) : ACL.SYSTEM,
            OFDomainRequirements(apiUri), null).isEmpty()) {
            return FormValidation.error("Cannot find currently selected credentials");
        }
        return FormValidation.ok();
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
                    CredentialsMatchers.allOf(CredentialsMatchers.withId(scanCredentialsId),
                        allUsernamePasswordMatch()));
        }
    }

    private static List<DomainRequirement> OFDomainRequirements(@CheckForNull String apiUri) {
        return URIRequirementBuilder.fromUri(StringUtils.defaultIfEmpty(apiUri, ORANGEFORGE_API_URL)).build();
    }

    public static CredentialsMatcher allUsernamePasswordMatch() {
        return CredentialsMatchers.anyOf(CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class));
    }
}
