package org.jenkinsci.plugins.tuleap_api.internals.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jenkinsci.plugins.tuleap_api.AccessKeyScope;
import org.jenkinsci.plugins.tuleap_api.User;
import org.jenkinsci.plugins.tuleap_api.UserApi;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEntity implements User {
    private String username;

    public UserEntity(@JsonProperty("username") String username) {
        this.username = username;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
