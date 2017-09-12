package com.francetelecom.faas.jenkinstuleapfaasbranchsource;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


import com.francetelecom.faas.jenkinstuleapfaasbranchsource.config.OrangeForgeSettings;

import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;


public class OFClient {

	private static final String API_PROJECT_PATH = "/projects";
	private static final String API_GIT_PATH = "/git";

	private OrangeForgeSettings orangeForgeSettings;

	private final OkHttpClient client;

	public OFClient(final OrangeForgeSettings orangeForgeSettings) {
		this.orangeForgeSettings = orangeForgeSettings;
		//FIXME enable cache later
		this.client = new OkHttpClient.Builder()
				.addInterceptor(new BasicAuthInterceptor(orangeForgeSettings.getUsername(),
														 orangeForgeSettings.getPassword()))
				.connectTimeout(10, TimeUnit.SECONDS)
				.writeTimeout(10, TimeUnit.SECONDS)
				.readTimeout(10, TimeUnit.SECONDS)
				.cache(null)
				.build();
	}

	public String getProject() {
		final String apiProjectsUrl = orangeForgeSettings.getApiBaseUrl() + API_PROJECT_PATH + "/" +
				orangeForgeSettings.getFaaSProjectId();
		//FIXME enable cache later
		Request req = new Request.Builder()
				.url(apiProjectsUrl)
				.addHeader("content-type", "application/json")
				.cacheControl(CacheControl.FORCE_NETWORK)
				.get()
				.build();
		try {
			//TODO Map from json to class representation
			return client.newCall(req).execute().body().string();
		} catch (IOException e) {
			//TODO Better handle error
			return "Error getting response from server : " + e.getMessage();
		}
	}

	public String getRepositories() {
		final String apiRepositoriesUrl = orangeForgeSettings.getApiBaseUrl() + API_PROJECT_PATH + "/" +
				orangeForgeSettings.getFaaSProjectId() + API_GIT_PATH;
		//FIXME enable cache later
		Request req = new Request.Builder()
				.url(HttpUrl.parse(apiRepositoriesUrl).newBuilder()
				.addQueryParameter("limit", "200").build())
				.addHeader("content-type", "application/json")
				.cacheControl(CacheControl.FORCE_NETWORK)
				.get()
				.build();
		try {
			//TODO Map from json to class representation
			return client.newCall(req).execute().body().string();
		} catch (IOException e) {
			//TODO Better handle error
			return "Error getting response from server : " + e.getMessage();
		}
	}
}
