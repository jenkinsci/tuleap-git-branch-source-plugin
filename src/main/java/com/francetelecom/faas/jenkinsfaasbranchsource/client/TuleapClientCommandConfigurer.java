package com.francetelecom.faas.jenkinsfaasbranchsource.client;


import com.cloudbees.plugins.credentials.common.StandardCredentials;

import hudson.ExtensionList;

public abstract class TuleapClientCommandConfigurer<T> {

	public static <T> TuleapClientCommandConfigurer<T> newInstance(String serverUrl) {
		for (TuleapClientCommandConfigurer factory : ExtensionList.lookup(TuleapClientCommandConfigurer.class)) {
			if (factory.isMatch(serverUrl)) {
				return factory.create(serverUrl);
			}
		}
		throw new IllegalArgumentException("Unsupported Tuleap server URL: " + serverUrl);
	}

	protected abstract boolean isMatch(String serverUrl);

	protected abstract TuleapClientCommandConfigurer<T> create(String apiUrl);

	public abstract TuleapClientCommandConfigurer<T> withCommand(TuleapClientRawCmd.Command<T> command);

	public abstract TuleapClientCommandConfigurer<T> withCredentials(StandardCredentials credentials);

	public abstract TuleapClientCommandConfigurer<T> withGitUrl(final String gitUrl);

	public abstract TuleapClientRawCmd.Command<T> configure();
}
