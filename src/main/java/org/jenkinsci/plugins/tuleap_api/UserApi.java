package org.jenkinsci.plugins.tuleap_api;

import hudson.util.Secret;

public interface UserApi {
    String USER_API = "/users";
    String USER_SELF_ID = "/self";

    User getUserForAccessKey(Secret secret);
}
