package org.jenkinsci.plugins.tuleap_credentials;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
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

    public void verifyAccessKey(String accessKey) throws InvalidAccessKeyException, InvalidScopesForAccessKeyException {
        if (! accessKeyIsValid(accessKey)) {
            throw new InvalidAccessKeyException();
        }

        if (! scopesAreValid(accessKey)) {
            throw new InvalidScopesForAccessKeyException();
        }
    }

    private Boolean accessKeyIsValid(String accessKey) {
        return client.checkAccessKeyIsValid(accessKey);
    }

    private Boolean scopesAreValid(String accessKey) {
        return client
        .getAccessKeyScopes(accessKey)
        .stream()
        .map(AccessKeyScope::getIdentifier)
        .collect(ImmutableList.toImmutableList())
        .containsAll(MANDATORY_SCOPES);
    }
}
