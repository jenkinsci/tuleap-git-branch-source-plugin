package org.jenkinsci.plugins.tuleap_credentials;

import com.google.inject.Inject;
import hudson.util.Secret;
import org.jenkinsci.plugins.tuleap_api.UserApi;

public class UsernameRetriever {
    private final UserApi client;

    @Inject
    public UsernameRetriever(final UserApi client) {
        this.client = client;
    }

    public String getUsernameForToken(final Secret token) {
        return client.getUserForAccessKey(token).getUsername();
    }
}
