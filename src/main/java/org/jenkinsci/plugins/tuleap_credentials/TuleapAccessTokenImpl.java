package org.jenkinsci.plugins.tuleap_credentials;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.Secret;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.tuleap_api.TuleapApiGuiceModule;
import org.jenkinsci.plugins.tuleap_credentials.exceptions.InvalidAccessKeyException;
import org.jenkinsci.plugins.tuleap_credentials.exceptions.InvalidScopesForAccessKeyException;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class TuleapAccessTokenImpl extends BaseStandardCredentials implements TuleapAccessToken {

    @NonNull
    private final Secret token;

    private String username;

    @DataBoundConstructor
    public TuleapAccessTokenImpl(
        @CheckForNull CredentialsScope scope,
        @CheckForNull String id,
        @CheckForNull String description,
        @NonNull String token
    ) {
        super(scope, id, description);
        this.token = Secret.fromString(token);
    }

    @Override
    @NonNull
    public Secret getToken() {
        return token;
    }

    @NonNull
    @Override
    public Secret getPassword() {
        return this.getToken();
    }

    @NonNull
    @Override
    public String getUsername() {
        if (username == null) {
            username = fetchUsername();
        }

        return username;
    }

    private String fetchUsername() {
        Injector injector = Guice.createInjector(new TuleapApiGuiceModule());
        UsernameRetriever retriever = injector.getInstance(UsernameRetriever.class);

        return retriever.getUsernameForToken(token);
    }

    @Extension
    @Symbol("tuleapAccessToken")
    public static class DescriptorImpl extends CredentialsDescriptor {
        @Override
        @NonNull
        public String getDisplayName() {
            return Messages.TuleapAccessToken_displayName();
        }

        @Restricted(NoExternalUse.class)
        public FormValidation doCheckToken(@QueryParameter String value) {
            Injector injector = Guice.createInjector(new TuleapApiGuiceModule());
            Secret secret = Secret.fromString(value);
            AccessKeyChecker checker = injector.getInstance(AccessKeyChecker.class);

            try {
                checker.verifyAccessKey(secret);
                return FormValidation.ok();
            } catch (InvalidAccessKeyException exception) {
                return FormValidation
                    .error(Messages.TuleapAccessToken_invalidAccessKey());
            } catch (InvalidScopesForAccessKeyException exception) {
                return FormValidation
                    .error(Messages.TuleapAccessToken_invalidScopesAccessKey());
            }
        }
    }
}
