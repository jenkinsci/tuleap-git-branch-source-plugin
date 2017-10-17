package com.francetelecom.faas.jenkinsfaasbranchsource;

import java.io.IOException;
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
import org.eclipse.jgit.util.StringUtils;
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
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFUser;

import static com.sun.javafx.font.FontResource.SALT;

import hudson.Util;
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
	private static final String API_USER_PATH = "/projects";
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
			final String hash = Util.getDigestOf(password + SALT);
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
		final String url = "https://www.forge.orange-labs.fr/api/users?query=%7B%22username%22%3A%20%22qsqf2513%22%7D";
		Request req = new Request.Builder()
				.url(url)
				.addHeader("content-type", "application/json")
				.cacheControl(CacheControl.FORCE_NETWORK)
				.get()
				.build();
		try (Response response = client.newCall(req).execute()) {
			if (!response.isSuccessful()) throw new IOException("HTTP call error at url: "+req.url().toString()+" " +
																		"with code: "+response.code());

			ResponseBody body = response.body();
			if (body != null) {
				/*com.fasterxml.jackson.databind.JsonMappingException: Can not deserialize instance of com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFUser out of START_ARRAY token
 at [Source: [{"email":"haja.rambelontsalama@orange.com","status":"A","id":18600,"uri":"users\/18600","user_url":"\/users\/qsqf2513","real_name":"RAMBELONTSALAMA Haja OBS\/OAB","display_name":"RAMBELONTSALAMA Haja OBS\/OAB (qsqf2513)","username":"qsqf2513","ldap_id":"QSQF2513","avatar_url":"https:\/\/www.forge.orange-labs.fr\/themes\/common\/images\/avatar_default.png","is_anonymous":false}]; line: 1, column: 1]*/
				return StringUtils.isEmptyOrNull(parse(body.string(), OFUser.class).getEmail());
			}
			return false;
		} catch (IOException e) {
			return false;
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
		//TODO Only return projects that current user is member of using ?query={"is_member_of": true}
		// From docs https://www.forge.orange-labs.fr/api/explorer/#!/projects/retrieve :
		// 		Please note that {"is_member_of": false} is not supported and will result in a 400 Bad Request error.
		// curl -L -u "qsqf2513" -X GET  https://www.forge.orange-labs.fr/api/projects?query=%7B%22is_member_of%22%3A%20true%7D
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
			final String hash = Util.getDigestOf(password + SALT);
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
