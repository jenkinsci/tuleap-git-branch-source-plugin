package org.jenkinsci.plugins.tuleap_api;

import com.google.common.collect.ImmutableList;
import hudson.util.Secret;

public interface AccessKeyApi {
    String ACCESS_KEY_API = "/access_keys";
    String ACCESS_KEY_SELF_ID = "/self";

    Boolean checkAccessKeyIsValid(Secret secret);

    ImmutableList<AccessKeyScope> getAccessKeyScopes(Secret secret);
}
