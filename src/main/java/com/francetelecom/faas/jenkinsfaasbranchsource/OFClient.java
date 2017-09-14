package com.francetelecom.faas.jenkinsfaasbranchsource;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.francetelecom.faas.jenkinsfaasbranchsource.config.OrangeForgeSettings;
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFProject;
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFProjectRepositories;

import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 *  OrangeForge REST client in charge of establishing connexion given some configuration.
 *  Translates OrangeForge api from api php object to api java object and populate api objects to prepare their usage
 *  with the SCM api.
 *  @see <a href=https://www.forge.orange-labs.fr/api/explorer/#!/git/retrieve>API OrangeForge</a>
 */
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

	public OFProject project() throws IOException {
		final String apiProjectsUrl = orangeForgeSettings.getApiBaseUrl() + API_PROJECT_PATH + "/" +
				orangeForgeSettings.getFaaSProjectId();
		//FIXME enable cache later
		Request req = new Request.Builder()
				.url(apiProjectsUrl)
				.addHeader("content-type", "application/json")
				.cacheControl(CacheControl.FORCE_NETWORK)
				.get()
				.build();
		try (Response response = client.newCall(req).execute()) {
			if (!response.isSuccessful()) throw new IOException("HTTP call error at url: "+req.url().toString()+" " +
																		"with code: "+response.code());
			return parse(response.body().string(), OFProject.class);
		} catch (IOException e) {
			throw new IOException("GetProject encounter error", e);
		}
	}

	public OFProjectRepositories projectRepositories() throws IOException {
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
		try (Response response = client.newCall(req).execute()) {
			if (!response.isSuccessful()) throw new IOException("HTTP call error at url: "+req.url().toString()+" " +
																		"with code: "+response.code());
			return parse(response.body().string(), OFProjectRepositories.class);
		} catch (IOException e) {
			throw new IOException("GetProjectRepositories encounter error", e);
		}
	}

	private <T> T parse (String input, Class<T> clazz) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(input, clazz);
		} catch (IOException e) {
			throw new IOException("Parsing class pbm", e);
		}
	}
}
