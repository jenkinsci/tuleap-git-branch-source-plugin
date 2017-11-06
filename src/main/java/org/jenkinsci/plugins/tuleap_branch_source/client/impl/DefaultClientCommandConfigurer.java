package org.jenkinsci.plugins.tuleap_branch_source.client.impl;

import java.util.Optional;


import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.tuleap_branch_source.client.TuleapClientCommandConfigurer;
import org.jenkinsci.plugins.tuleap_branch_source.client.TuleapClientRawCmd;
import org.jenkinsci.plugins.tuleap_branch_source.config.TuleapConfiguration;

import com.cloudbees.plugins.credentials.common.StandardCredentials;

import hudson.Extension;

@Extension
public class DefaultClientCommandConfigurer<T> implements TuleapClientCommandConfigurer {

    private String apiUrl;
    private String gitUrl;
    private TuleapClientRawCmd.Command command;
    private StandardCredentials credentials;

    public DefaultClientCommandConfigurer() {
    }

    private DefaultClientCommandConfigurer(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @Override
    public final boolean isMatch(String apiUrl) {
        // FIXME now just returns true :(
        return apiUrl.equals(TuleapConfiguration.get().getApiBaseUrl());
    }

    @Override
    public final DefaultClientCommandConfigurer<T> create(String apiUrl) {
        return new DefaultClientCommandConfigurer<>(apiUrl);
    }

    @Override
    public final DefaultClientCommandConfigurer<T> withCommand(TuleapClientRawCmd.Command command) {
        this.command = command;
        return this;
    }

    @Override
    public final DefaultClientCommandConfigurer<T> withCredentials(StandardCredentials credentials) {
        this.credentials = credentials;
        return this;
    }

    @Override
    public final DefaultClientCommandConfigurer<T> withGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
        return this;
    }

    @Override
    public final TuleapClientRawCmd.Command<T> configure() {
        DefaultClient client = new DefaultClient(Optional.ofNullable(credentials), StringUtils
            .defaultString(apiUrl), StringUtils.defaultString(gitUrl));
        ((TuleapClientRawCmd) command).setClient(client);
        return command;
    }
}
