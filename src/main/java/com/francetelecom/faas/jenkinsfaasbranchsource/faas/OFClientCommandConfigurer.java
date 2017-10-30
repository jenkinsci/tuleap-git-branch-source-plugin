package com.francetelecom.faas.jenkinsfaasbranchsource.faas;

import org.apache.commons.lang.StringUtils;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.TuleapClientCommandConfigurer;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.TuleapClientRawCmd;
import com.francetelecom.faas.jenkinsfaasbranchsource.config.TuleapConfiguration;

import hudson.Extension;

@Extension
public class OFClientCommandConfigurer<T> extends TuleapClientCommandConfigurer {

    private String apiUrl;
    private String gitUrl;
    private TuleapClientRawCmd.Command command;
    private StandardCredentials credentials;

    public OFClientCommandConfigurer() {
    }

    private OFClientCommandConfigurer(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @Override
    protected boolean isMatch(String apiUrl) {
        // FIXME now just returns true :(
        return apiUrl.equals(TuleapConfiguration.get().getApiBaseUrl());
    }

    @Override
    protected OFClientCommandConfigurer<T> create(String apiUrl) {
        return new OFClientCommandConfigurer<>(apiUrl);
    }

    @Override
    public OFClientCommandConfigurer<T> withCommand(TuleapClientRawCmd.Command command) {
        this.command = command;
        return this;
    }

    @Override
    public OFClientCommandConfigurer<T> withCredentials(StandardCredentials credentials) {
        this.credentials = credentials;
        return this;
    }

    @Override
    public OFClientCommandConfigurer<T> withGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
        return this;
    }

    @Override
    public TuleapClientRawCmd.Command<T> configure() {
        OFClient client = new OFClient(credentials != null ? credentials : null, StringUtils.defaultString(apiUrl),
            StringUtils.defaultString(gitUrl));
        ((TuleapClientRawCmd) command).setClient(client);
        return command;
    }
}
