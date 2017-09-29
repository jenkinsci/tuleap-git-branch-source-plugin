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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.francetelecom.faas.jenkinsfaasbranchsource.config.BasicAuthInterceptor;
import com.francetelecom.faas.jenkinsfaasbranchsource.config.OrangeForgeSettings;
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFGitBranch;
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFGitCommit;
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

	public OFProject configuredProject() throws IOException {
		return projectById(orangeForgeSettings.getFaaSProjectId());

	}

	public OFProject projectById(String projectId) throws IOException {
		final String apiProjectsUrl = orangeForgeSettings.getApiBaseUrl() + API_PROJECT_PATH + "/" + projectId;
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
	public List<OFGitRepository> projectRepositories() throws IOException {
		return projectRepositoriesWrapper().getRepositories();
	}

	/**
	 * Get repositories wrapper of the configured orangeForge.properties project from OrangeForge api
	 * @return the repositories wrapper {@see OFProjectRepositories}
	 * @throws IOException in case HTTP errors occurs or parsing of response fail
	 */
	private OFProjectRepositories projectRepositoriesWrapper() throws IOException {
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

	public List<OFGitBranch> branchByGitRepo (final String gitRepoPath) throws
			IOException, NoSingleRepoByPathException, NoSuchElementException {
		try {
			LOGGER.info("Ls-remoting heads of git repository at {} + {}",
						orangeForgeSettings.getGitBaseUrl(), gitRepoPath);
			return new LsRemoteCommand(null)
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
							orangeForgeSettings.getUsername(), orangeForgeSettings.getPassword()))
					.setRemote(orangeForgeSettings.getGitBaseUrl()+ gitRepoPath)
					.setHeads(true)
					.call()
					.stream()
					.map(refToOFGitBranch())
					.collect(Collectors.toList());
		} catch (GitAPIException e) {
			throw new OFGitException(orangeForgeSettings.getGitBaseUrl(), gitRepoPath, e);
		}
	}

	private Optional<OFGitRepository> gitRepoByPath(final String gitRepoPath) throws IOException,
			NoSingleRepoByPathException {
		return projectRepositories().stream()
					.filter(ofGitRepository -> gitRepoPath.equals(ofGitRepository.getPath()))
					.reduce((a,b) -> {
						throw new NoSingleRepoByPathException(gitRepoPath, a.getUri(), b.getUri());
					});
	}

	private Function<OFGitRepository, List<OFGitBranch>> headsInRepo() {
		return ofGitRepository -> {
			try {
				LOGGER.info("Ls-remoting heads of git repository at {} + {}",
							orangeForgeSettings.getGitBaseUrl(),
							ofGitRepository.getPath());
				return new LsRemoteCommand(null)
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
								orangeForgeSettings.getUsername(), orangeForgeSettings.getPassword()))
						.setRemote(orangeForgeSettings.getGitBaseUrl()+ofGitRepository.getPath())
						.setHeads(true)
						.call()
						.stream()
						.map(refToOFGitBranch())
						.collect(Collectors.toList());
			} catch (GitAPIException e) {
				throw new OFGitException(ofGitRepository.getUri(), /*ofGitRepository.getName(),*/ ofGitRepository.getPath(), e);
			}
		};
	}

	private Function<Ref, OFGitBranch> refToOFGitBranch() {
		return ref -> new OFGitBranch(ref.getName(), ref.getObjectId().getName());
	}

	private <T> T parse (String input, Class<T> clazz) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(input, clazz);
		} catch (IOException e) {
			throw new IOException("Parsing class pbm", e);
		}
	}

	public OFGitCommit resolveCommit(String sha1, String repoPath) {
		/*Repository repo ;
		GitClient cleint;
		//new org.jenkinsci.plugins.gitclient.RemoteGitImpl();
		ArchiveCommand a = new ArchiveCommand(null);
		try {
			final LsRemoteCommand lsRemoteCommand = new LsRemoteCommand(null)
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider(
							orangeForgeSettings.getUsername(), orangeForgeSettings.getPassword()))
					.setRemote(orangeForgeSettings.getGitBaseUrl() + repoPath);
			lsRemoteCommand.call();
			Repository r = lsRemoteCommand.getRepository();
			ObjectId id = ObjectId.fromString(sha1);
			Git g = Git.init().call();
			g.remoteSetUrl().setUri(new URIish(orangeForgeSettings.getGitBaseUrl()+repoPath));
			CloneCommand c ;
			RevWalk walk = new RevWalk(r);
			RevCommit c = walk.parseCommit(id);
			return new OFGitCommit(c.getShortMessage(), sha1, c.getCommitTime());
		} catch (IOException e) {
			throw new OFGitException("","", e);
		} catch (InvalidRemoteException e) {
			throw new OFGitException("","", e);

		} catch (TransportException e) {
			throw new OFGitException("","", e);

		} catch (GitAPIException e) {
			throw new OFGitException("","", e);

		} catch (URISyntaxException e) {
			throw new OFGitException("","", e);
		}*/
		return new OFGitCommit("sample commit msg", sha1, 0);
	}

	private static class NoSingleRepoByPathException extends RuntimeException {

		private NoSingleRepoByPathException(String path, String doublonUri, String anotherDoublonUri) {
			super("Multiple repository with path '"+path+"' :"+doublonUri+" and "+anotherDoublonUri);
		}
	}

	private static class OFGitException extends RuntimeException {
		private OFGitException(String uri, String path, Throwable t) {
			super("Unable to communicate to OrangeForge git at "+uri+"/"+path, t);
		}
	}
}
