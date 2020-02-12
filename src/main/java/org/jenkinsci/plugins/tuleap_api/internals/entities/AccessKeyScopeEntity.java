package org.jenkinsci.plugins.tuleap_api.internals.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jenkinsci.plugins.tuleap_api.AccessKeyScope;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessKeyScopeEntity implements AccessKeyScope {
    private String identifier;

    public AccessKeyScopeEntity(@JsonProperty("identifier") String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }
}
