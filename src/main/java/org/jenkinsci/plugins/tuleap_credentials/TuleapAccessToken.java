package org.jenkinsci.plugins.tuleap_credentials;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.util.Secret;

public interface TuleapAccessToken extends StandardCredentials {

    /**
     * @return the token.
     */
    @NonNull
    Secret getToken();
}

