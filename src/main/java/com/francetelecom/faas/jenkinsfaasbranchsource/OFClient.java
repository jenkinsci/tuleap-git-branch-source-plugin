package com.francetelecom.faas.jenkinsfaasbranchsource;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;


import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.francetelecom.faas.jenkinsfaasbranchsource.config.BasicAuthInterceptor;
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFGitBranch;
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFGitRepository;
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFProject;
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFProjectRepositories;

import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 *  OrangeForge REST client in charge of establishing connexion given some configuration.
 *  Translates OrangeForge api from api php object to api java object and populate api objects to prepare their usage
 *  with the SCM api.
 *  @see <a href=https://www.forge.orange-labs.fr/api/explorer/#!/git/retrieve>API OrangeForge</a>
 */
public class OFClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(OFClient.class);
	private static final String API_PROJECT_PATH = "/projects";
	private static final String API_USER_PATH = "/users";
	private static final String API_GIT_PATH = "/git";
	private final String apiBaseUrl, gitBaseUrl;
	private StandardUsernamePasswordCredentials credentials;

	private final OkHttpClient client;

	public OFClient(StandardCredentials credentials, final String apiBaseUrl, final String gitBaseUrl) {
		if (credentials instanceof StandardUsernamePasswordCredentials) {
			this.apiBaseUrl = apiBaseUrl;
			this.gitBaseUrl = gitBaseUrl;
			this.credentials = (StandardUsernamePasswordCredentials) credentials;
			final String username = this.credentials.getUsername();
			final String password = this.credentials.getPassword().getPlainText();
			this.client = new OkHttpClient.Builder()
					.addInterceptor(new BasicAuthInterceptor(username, password))
					.connectTimeout(10, TimeUnit.SECONDS)
					.writeTimeout(10, TimeUnit.SECONDS)
					.readTimeout(10, TimeUnit.SECONDS)
					.cache(null)
					.build();
		} else {
			throw new UnsupportedOperationException("TODO implement/contribute");
		}
	}

	public boolean isCredentialValid() throws IOException {
		final String queryObject = "{\"username\":\""+credentials.getUsername()+"\"}";
		final String urlEncodedQueryObject = URLEncoder.encode(queryObject, StandardCharsets.UTF_8.displayName());
		final String userApiUrl = apiBaseUrl+ API_USER_PATH + "?query="+urlEncodedQueryObject;
		Request req = new Request.Builder()
				.url(userApiUrl)
				.addHeader("content-type", "application/json")
				.cacheControl(CacheControl.FORCE_NETWORK)
				.get()
				.build();
		try (Response response = client.newCall(req).execute()) {
			return response.isSuccessful();
		}
	}

	/**
	 * Return current user's projects using param object ?query={"is_member_of": true} of OrangeForge api
	 * From docs https://www.forge.orange-labs.fr/api/explorer/#!/projects/retrieve :
	 * Please note that {"is_member_of": false} is not supported and will result in a 400 Bad Request error.
	 * @return
	 * @throws IOException
	 */
	public List<OFProject> userProjects() throws IOException {
		final String queryObject = "{\"is_member_of\":true}";
		final String urlEncodedQueryObject = URLEncoder.encode(queryObject, StandardCharsets.UTF_8.displayName());
		final String projectsApiUrl = apiBaseUrl+ API_PROJECT_PATH + "?query="+urlEncodedQueryObject;
		Request req = new Request.Builder()
				.url(projectsApiUrl)
				.addHeader("content-type", "application/json")
				.cacheControl(CacheControl.FORCE_NETWORK)
				.get()
				.build();
		try (Response response = client.newCall(req).execute()) {
			if (!response.isSuccessful()) throw new IOException("HTTP call error at url: "+req.url().toString()+" " +
																		"with code: "+response.code());

			ResponseBody body = response.body();
			if (body != null) {
				ObjectMapper mapper = new ObjectMapper();
				final OFProject[] projects = mapper.readValue(body.string(), OFProject[].class);
				return Arrays.asList(projects);
			}
			return null;
		} catch (IOException e) {
			throw new IOException("Retrieve current user's projects encounter error", e);
		}
	}

	public OFProject projectById(final String  projectId) throws IOException {
		final String apiProjectsUrl = apiBaseUrl + API_PROJECT_PATH + "/" + projectId;
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

			ResponseBody body = response.body();
			if (body != null) {
				return parse(body.string(), OFProject.class);
			}
			return null;
		} catch (IOException e) {
			throw new IOException("GetProject encounter error", e);
		}
	}

	/**
	 * Get a list of repositories in the configured orangeForge.properties project
	 * @return the list of repositories
	 * @throws IOException in case HTTP errors occurs or parsing of response fail
	 */
	public List<OFGitRepository> projectRepositories(final String  projectId) throws IOException {
		return projectRepositoriesWrapper(projectId).getRepositories();
	}

	/**
	 * Get repositories wrapper of the configured orangeForge.properties project from OrangeForge api
	 * @return the repositories wrapper {@see OFProjectRepositories}
	 * @throws IOException in case HTTP errors occurs or parsing of response fail
	 */
	private OFProjectRepositories projectRepositoriesWrapper(final String  projectId) throws IOException {
		final String apiRepositoriesUrl = apiBaseUrl + API_PROJECT_PATH + "/" + projectId + API_GIT_PATH;
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

	public List<OFGitBranch> branchByGitRepo (final String gitRepoPath) throws
			IOException, NoSingleRepoByPathException, NoSuchElementException {
		try {
			LOGGER.info("Ls-remoting heads of git repository at {} + {}", gitBaseUrl, gitRepoPath);
			final String username = this.credentials.getUsername();
			final String password = this.credentials.getPassword().getPlainText();
			return new LsRemoteCommand(null)
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
					.setRemote(gitBaseUrl+ gitRepoPath)
					.setHeads(true)
					.call()
					.stream()
					.map(refToOFGitBranch())
					.collect(Collectors.toList());
		} catch (GitAPIException e) {
			throw new OFGitException(gitBaseUrl, gitRepoPath, e);
		}
	}

	private Optional<OFGitRepository> gitRepoByPath(final String projectId, final String gitRepoPath) throws
			IOException,
			NoSingleRepoByPathException {
		return projectRepositories(projectId).stream()
					.filter(ofGitRepository -> gitRepoPath.equals(ofGitRepository.getPath()))
					.reduce((a,b) -> {
						throw new NoSingleRepoByPathException(gitRepoPath, a.getUri(), b.getUri());
					});
	}

	private Function<Ref, OFGitBranch> refToOFGitBranch() {
		return ref -> new OFGitBranch(ref.getName(), ref.getObjectId().getName());
	}

	private <T> T parse (final String  input, Class<T> clazz) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(input, clazz);
		} catch (IOException e) {
			throw new IOException("Parsing class pbm", e);
		}
	}

	private static class NoSingleRepoByPathException extends RuntimeException {

		private NoSingleRepoByPathException(final String path, final String  doublonUri, final String  anotherDoublonUri) {
			super("Multiple repository with path '"+path+"' :"+doublonUri+" and "+anotherDoublonUri);
		}
	}

	private static class OFGitException extends RuntimeException {
		private OFGitException(final String uri, final String  path, Throwable t) {
			super("Unable to communicate to OrangeForge git at "+uri+"/"+path, t);
		}
	}
}
