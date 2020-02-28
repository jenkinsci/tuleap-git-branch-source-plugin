package org.jenkinsci.plugins.tuleap_credentials;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.util.Secret;

public interface TuleapAccessToken extends StandardUsernamePasswordCredentials {

    /**
     * @return the token.
     */
    @NonNull
    Secret getToken();
}

