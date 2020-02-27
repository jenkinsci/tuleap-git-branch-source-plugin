package org.jenkinsci.plugins.tuleap_git_branch_source.config;

import java.util.Collections;
import java.util.List;
import java.util.Objects;


import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jenkinsci.plugins.tuleap_api.TuleapApiGuiceModule;
import org.jenkinsci.plugins.tuleap_credentials.AccessKeyChecker;
import org.jenkinsci.plugins.tuleap_credentials.TuleapAccessToken;
import org.jenkinsci.plugins.tuleap_credentials.exceptions.InvalidAccessKeyException;
import org.jenkinsci.plugins.tuleap_credentials.exceptions.InvalidScopesForAccessKeyException;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
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
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

public class TuleapConnector {

    public static ListBoxModel listScanCredentials(@CheckForNull @AncestorInPath Item context,
        @QueryParameter String apiUri, @QueryParameter String credentialsId, boolean includeEmpty) {
        if (context == null ?
                !Jenkins.get().hasPermission(Jenkins.ADMINISTER) :
                !context.hasPermission(Item.EXTENDED_READ)) {
            return new StandardListBoxModel().includeCurrentValue(credentialsId);
        }
        final StandardListBoxModel model = new StandardListBoxModel();
        if (includeEmpty) {
            model.includeEmptyValue();
        }
        return model.includeMatchingAs(
            context instanceof Queue.Task ? ((Queue.Task) context).getDefaultAuthentication() : ACL.SYSTEM,
            context, TuleapAccessToken.class, TuleapDomainRequirements(apiUri), allTuleapAccessTokenMatch());
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE") // see https://github.com/spotbugs/spotbugs/issues/651
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
            return FormValidation.error("Tuleap Access Token is required !");
        } else {
            Injector injector = Guice.createInjector(new TuleapApiGuiceModule());
            AccessKeyChecker checker = injector.getInstance(AccessKeyChecker.class);
            TuleapAccessToken token = lookupScanCredentials(item, apiUri, credentialsId);
            try {
                checker.verifyAccessKey(Objects.requireNonNull(token).getToken());
                return FormValidation.ok();
            } catch (InvalidAccessKeyException | InvalidScopesForAccessKeyException exception) {
                return FormValidation.error("Failed to validate the provided credentials");
            }
        }
    }

    @CheckForNull
    public static TuleapAccessToken lookupScanCredentials(@CheckForNull Item context, @CheckForNull String apiUri,
        @CheckForNull String scanCredentialsId) {
        if (Util.fixEmpty(scanCredentialsId) == null) {
            return null;
        } else {
            return CredentialsMatchers
                .firstOrNull(
                    CredentialsProvider.lookupCredentials(TuleapAccessToken.class, context,
                        context instanceof Queue.Task ? ((Queue.Task) context).getDefaultAuthentication()
                            : ACL.SYSTEM,
                        TuleapDomainRequirements(apiUri)),
                    CredentialsMatchers.allOf(withId(scanCredentialsId),
                                              allTuleapAccessTokenMatch()));
        }
    }

    private static List<DomainRequirement> TuleapDomainRequirements(@CheckForNull String apiUri) {
        return URIRequirementBuilder.fromUri(defaultIfEmpty(apiUri, TuleapConfiguration.get().getApiBaseUrl())).build();
    }

    private static CredentialsMatcher allTuleapAccessTokenMatch() {
        return CredentialsMatchers.anyOf(CredentialsMatchers.instanceOf(TuleapAccessToken.class));
    }
}
