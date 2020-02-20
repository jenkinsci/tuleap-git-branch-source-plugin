package org.jenkinsci.plugins.tuleap_credentials;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import hudson.util.Secret;
import org.jenkinsci.plugins.tuleap_api.AccessKeyApi;
import org.jenkinsci.plugins.tuleap_api.AccessKeyScope;
import org.jenkinsci.plugins.tuleap_credentials.exceptions.InvalidAccessKeyException;
import org.jenkinsci.plugins.tuleap_credentials.exceptions.InvalidScopesForAccessKeyException;

public class AccessKeyChecker {
    private static ImmutableList<String> MANDATORY_SCOPES = ImmutableList.of("write:rest", "write:git_repository");

    private AccessKeyApi client;

    @Inject
    public AccessKeyChecker(AccessKeyApi client) {
        this.client = client;
    }

    public void verifyAccessKey(Secret secret) throws InvalidAccessKeyException, InvalidScopesForAccessKeyException {
        if (! accessKeyIsValid(secret)) {
            throw new InvalidAccessKeyException();
        }

        if (! scopesAreValid(secret)) {
            throw new InvalidScopesForAccessKeyException();
        }
    }

    private Boolean accessKeyIsValid(Secret secret) {
        return client.checkAccessKeyIsValid(secret);
    }

    private Boolean scopesAreValid(Secret secret) {
        return client
        .getAccessKeyScopes(secret)
        .stream()
        .map(AccessKeyScope::getIdentifier)
        .collect(ImmutableList.toImmutableList())
        .containsAll(MANDATORY_SCOPES);
    }
}
