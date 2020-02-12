package org.jenkinsci.plugins.tuleap_api;

import com.google.common.collect.ImmutableList;

public interface AccessKeyApi {
    String ACCESS_KEY_API = "/access_keys";
    String SELF_ID = "/self";

    Boolean checkAccessKeyIsValid(String accessKey);

    ImmutableList<AccessKeyScope> getAccessKeyScopes(String accessKey);
}
