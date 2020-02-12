package org.jenkinsci.plugins.tuleap_api.internals.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessKeyEntity {
    private ImmutableList<AccessKeyScopeEntity> scopes;

    public AccessKeyEntity(
        @JsonProperty("scopes") ImmutableList<AccessKeyScopeEntity> scopes
    ) {
        this.scopes = scopes;
    }

    public ImmutableList<AccessKeyScopeEntity> getScopes() {
        return Optional.ofNullable(scopes).orElse(ImmutableList.of());
    }
}
