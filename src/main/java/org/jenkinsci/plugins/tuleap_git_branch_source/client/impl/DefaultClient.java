package org.jenkinsci.plugins.tuleap_git_branch_source.client.impl;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.TaskListener;
import okhttp3.*;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.RemoteRepositoryException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jenkinsci.plugins.tuleap_git_branch_source.client.TuleapClient;
import org.jenkinsci.plugins.tuleap_git_branch_source.client.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * A default implementation of a Tuleap Client
 */
class DefaultClient implements TuleapClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultClient.class);
    private final String apiBaseUrl, gitBaseUrl;
    private final OkHttpClient client;
    private Optional<StandardCredentials> credentials;
    private Optional<TaskListener> listener;

    DefaultClient(Optional<StandardCredentials> credentials, final String apiBaseUrl, final String gitBaseUrl,
                  Optional<TaskListener> listener) {
        this.apiBaseUrl = apiBaseUrl;
        this.gitBaseUrl = gitBaseUrl;
        this.credentials = credentials;
        this.listener = listener;
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit
                .SECONDS).cache(null);
        if (this.credentials.isPresent()) {
            final StandardCredentials c = this.credentials.get();
            if (c instanceof StandardUsernamePasswordCredentials) {
                final String username = ((StandardUsernamePasswordCredentials) c).getUsername();
                final String password = ((StandardUsernamePasswordCredentials) c).getPassword().getPlainText();
                builder.addInterceptor(new BasicAuthInterceptor(username, password));
            } else {
                throw new UnsupportedOperationException(
                    "Not implemented yet, only StandardUsernamePasswordCredentials " + "is supported ... for the moment");
            }
        }
        this.client = builder.build();
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isCredentialValid() throws IOException {
        isApiUrlPresent("Checking credentials");
        isCredentialsPresent("Checking credentials");

        final String username = ((StandardUsernamePasswordCredentials) credentials.get()).getUsername();
        final String queryObject = String.format(BY_USERNAME_QUERY_OBJECT_PATTERN, username);
        final String urlEncodedQueryObject = URLEncoder.encode(queryObject, StandardCharsets.UTF_8.displayName());
        final String userApiUrl = apiBaseUrl + TULEAP_API_USER_PATH + QUERY_OBJECT_PARAM + urlEncodedQueryObject;
        Request req = new Request.Builder()
            .url(userApiUrl)
            .addHeader("content-type", "application/json")
            .cacheControl(CacheControl.FORCE_NETWORK)
            .get()
            .build();
        try (Response response = client.newCall(req).execute()) {
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                if (body != null) {
                    List<TuleapUser> users = Arrays.asList((parse(body.string(), TuleapUser[].class)));
                    if (users.size() > 1){
                        throw new MultipleUserMatchingCredentialsException(users);
                    }
                    return isNotBlank(users.get(0).getEmail()) && isNotBlank(users.get(0)
                        .getStatus());
                }
            } else {
                throw new IOException(
                    "HTTP call error at url: " + req.url().toString() + " " + "with code: " + response.code());
            }
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isServerUrlValid() throws IOException {
        isApiUrlPresent("Checking url");
        String apiExplorerUrl = apiBaseUrl + TULEAP_API_EXPLORER_PATH ;
        Request req = new Request.Builder()
            .url(apiExplorerUrl)
            .addHeader("content-type", "application/json")
            .cacheControl(CacheControl.FORCE_NETWORK)
            .get()
            .build();
        try (Response response = client.newCall(req).execute()) {
            if (!response.isSuccessful())
                throw new IOException(
                    "HTTP call error at url: " + req.url().toString() + " " + "with code: " + response.code());

            ResponseBody body = response.body();
            if (body != null) {
//                TuleapApi api = parse(body.string(), TuleapApi.class);
                //API has changed no version number provided anymore :(
                //return "1".equals(api.getApiVersion());
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new IOException("Retrieve current api encounter error", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final Stream<TuleapProject> allUserProjects(boolean isMemberOf) throws IOException {
        isApiUrlPresent("Fetching all users's projects");
        LOGGER.info("Fetching all user's projects");
        String projectsApiUrl = apiBaseUrl + TULEAP_API_PROJECT_PATH ;
        //If property is_member_of is not defined, api will respond with all projects in read-only mode
        if (isMemberOf) {
            final String urlEncodedQueryObject = URLEncoder.encode(IS_MEMBER_OF_QUERY_OBJECT_PATTERN, StandardCharsets.UTF_8.displayName());
            projectsApiUrl += QUERY_OBJECT_PARAM + urlEncodedQueryObject;
        }
        int offset            = 0;
        int limit             = 50;
        int totalPages        = 0;
        int pageCount         = 0;
        Stream<TuleapProject> allProjects = Stream.empty();
        do {
            offset = pageCount * limit;
            final String fetchUrl = projectsApiUrl + "&offset=" + offset + "&limit=" + limit;
            LOGGER.info("GET {}", fetchUrl);

            Request req = new Request.Builder()
                .url(fetchUrl)
                .addHeader("content-type", "application/json")
                .cacheControl(CacheControl.FORCE_NETWORK)
                .get()
		.build();
	    try (Response response = client.newCall(req).execute()) {
		if (offset == 0) {
		    int nbProjectsMax = Integer.parseInt(response.header(COLLECTION_LENGTH_HEADER));
		    totalPages = nbProjectsMax / limit + ((nbProjectsMax % limit == 0) ? 0 : 1);
		}
		if (!response.isSuccessful())
		    throw new IOException(
			    "HTTP call error at url: " + req.url().toString() + " " + "with code: " + response.code());

                ResponseBody body = response.body();
                if (body != null) {
                    allProjects = Stream.concat(allProjects, Stream.of(parse(body.string(), TuleapProject[].class)));
                }
            } catch (IOException e) {
                throw new IOException("Retrieve current user's projects encounter error", e);
            }
            pageCount++;
        } while(pageCount < totalPages);
        return allProjects;
    }

    /**
     * {@inheritDoc}
     */
    public final Optional<TuleapProject> projectById(final String projectId) throws IOException {
        isApiUrlPresent("Fetching project by Id");
        //avoid falling back to TULEAP_API_PROJECT_PATH if no projectId
        isProjectIdPresent(projectId,"Fetching project by Id");
        final String apiProjectsUrl = apiBaseUrl + TULEAP_API_PROJECT_PATH + "/" + projectId;
        // FIXME enable cache later
        Request req = new Request.Builder()
            .url(apiProjectsUrl)
            .addHeader("content-type", "application/json")
            .cacheControl(CacheControl.FORCE_NETWORK)
            .get()
            .build();
        try (Response response = client.newCall(req).execute()) {
            if (!response.isSuccessful())
                throw new IOException(
                    "HTTP call error at url: " + req.url().toString() + " " + "with code: " + response.code());

            ResponseBody body = response.body();
            if (body != null) {
                return Optional.ofNullable(parse(body.string(), TuleapProject.class));
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new IOException("GetProject encounter error", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final Stream<TuleapGitRepository> allProjectRepositories(final String projectId) throws IOException {
        return projectRepositoriesWrapper(projectId).getRepositories().stream();
    }

    /**
     * Get git repositories wrapper per api contract.
     *
     * @param projectId the project id to inspect
     * @return the repositories wrapper {@see OFProjectRepositories}
     * @throws IOException in case HTTP errors occurs or parsing of response fail
     */
    private TuleapProjectRepositories projectRepositoriesWrapper(final String projectId) throws IOException {
        isApiUrlPresent("Fetching all project's git repos");
        isProjectIdPresent(projectId, "Fetching all project's git repos");
        final String apiRepositoriesUrl = apiBaseUrl + TULEAP_API_PROJECT_PATH + "/" + projectId + TULEAP_API_GIT_PATH;
        // FIXME enable cache later
        Request req = new Request.Builder()
            .url(HttpUrl.parse(apiRepositoriesUrl).newBuilder().addQueryParameter("limit", "200").build())
            .addHeader("content-type", "application/json")
            .cacheControl(CacheControl.FORCE_NETWORK)
            .get()
            .build();
        try (Response response = client.newCall(req).execute()) {
            if (!response.isSuccessful())
                throw new IOException(
                    "HTTP call error at url: " + req.url().toString() + " " + "with code: " + response.code());
            return parse(response.body().string(), TuleapProjectRepositories.class);
        } catch (IOException e) {
            throw new IOException("GetProjectRepositories encounter error", e);
        }
    }

    public final Stream<TuleapBranches> allBranches(int idRepo) throws IOException {
        String allBranchesUrl = apiBaseUrl + TULEAP_API_GIT_PATH + "/" + idRepo + "/branches";
        int offset            = 0;
        int limit             = 50;
        int totalPages        = 0;
        int pageCount         = 0;
        Stream<TuleapBranches> allBranches = Stream.empty();
        do {
            offset = pageCount * limit;
            final String fetchUrl = allBranchesUrl + "?offset=" + offset + "&limit=" + limit;
            LOGGER.info("GET {}", fetchUrl);

            Request request = new Request.Builder()
                .url(new URL(allBranchesUrl))
                .addHeader("content-type", "application/json")
                .cacheControl(CacheControl.FORCE_NETWORK)
                .get()
                .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException(
                        "HTTP call error at url: " + request.url().toString() + " " + "with code: " + response.code());
                }
                if (offset == 0) {
		    int nbMax = Integer.parseInt(response.header(COLLECTION_LENGTH_HEADER));
		    totalPages = nbMax / limit + ((nbMax % limit == 0) ? 0 : 1);
		}
                allBranches = Stream.concat(allBranches, Stream.of(parse(response.body().string(), TuleapBranches[].class)));
            } catch (IOException e) {
                throw new IOException("GetBranches encounter error", e);
            }
            pageCount++;
        } while(pageCount < totalPages);
        return allBranches;
    }

    public final Optional<TuleapFileContent> getJenkinsFile(int idRepo, String pathToFile, String ref) throws IOException {
        String getJenkinsFileUrl = apiBaseUrl + TULEAP_API_GIT_PATH + "/" + idRepo + "/files";
        Request request = new Request.Builder().url(HttpUrl.parse(getJenkinsFileUrl).newBuilder()
            .addQueryParameter("path_to_file", pathToFile)
            .addQueryParameter("ref", ref).build())
            .addHeader("content-type", "application/json")
            .cacheControl(CacheControl.FORCE_NETWORK)
            .get()
            .build();
        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (body != null) {
                return Optional.ofNullable(parse(body.string(), TuleapFileContent.class));
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new IOException("getJenkinsFile encounter error", e);
        }
    }
    /**
     * {@inheritDoc}
     */
    public final Stream<TuleapGitBranch> branchByGitRepo(String gitRepoPath, String projectName)
        throws NoSingleRepoByPathException, TuleapGitException {
        isGitUrlPresent("Fetching git repo's branches");
        isCredentialsPresent("Fetching git repo's branches");
        if (isEmpty(gitRepoPath)) {
            throw new IllegalArgumentException("Fetching git repo's branches requires a git repo path but is missing");
        }
        try {
            LOGGER.info("Ls-remoting heads of git repository at {} + {}", gitBaseUrl, gitRepoPath);
            final String username = ((StandardUsernamePasswordCredentials)credentials.get()).getUsername();
            final String password = ((StandardUsernamePasswordCredentials)credentials.get()).getPassword().getPlainText();
            if (!startsWith(gitRepoPath, projectName+"/")) {
                gitRepoPath = projectName+"/" + gitRepoPath;
            }
            if (!endsWith(gitRepoPath, ".git")) {
                gitRepoPath = gitRepoPath + ".git";
            }
            final String remote = gitBaseUrl + gitRepoPath;
            return listRemoteBranch(username, password, remote)
                .stream()
                .map(refToOFGitBranch());
        } catch (GitAPIException e) {
            throw new TuleapGitException(gitBaseUrl, gitRepoPath, e);
        }
    }

    private Collection<Ref> listRemoteBranch(String username, String password, String remote) throws GitAPIException, TuleapGitException {
        try {
            return new LsRemoteCommand(null)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                .setRemote(remote)
                .setHeads(true)
                .call();
        } catch (TransportException e) {
            if (e.getCause() instanceof RemoteRepositoryException && e.getMessage().contains("DENIED by fallthru")) {
                listener.ifPresent(l -> l.getLogger().printf("It seems user : %s is denied access to the repository :" +
                                                                 "%s %n%s%n", username, remote, e.getMessage()));
                LOGGER.warn("Permission denied to access this repository\n" + e.getMessage());
                return Collections.EMPTY_LIST;
            } else {
                throw new TuleapGitException(remote, e);
            }
        }
    }

    private Optional<TuleapGitRepository> gitRepoByPath(final String projectId, final String gitRepoPath)
        throws IOException, NoSingleRepoByPathException {
        return allProjectRepositories(projectId)
            .filter(ofGitRepository -> gitRepoPath.equals(ofGitRepository.getPath()))
            .reduce((a, b) -> {
                throw new NoSingleRepoByPathException(gitRepoPath, a.getUri(), b.getUri());
            });
    }

    private Function<Ref, TuleapGitBranch> refToOFGitBranch() {
        return ref -> new TuleapGitBranch(StringUtils.removeStart(ref.getName(), "refs/heads/"),
                                          ref.getObjectId().getName());
    }

    private <T> T parse(final String input, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(input, clazz);
        } catch (IOException e) {
            throw new IOException("Parsing class pbm", e);
        }
    }

    private void isApiUrlPresent(String message) {
        if (isEmpty(apiBaseUrl)) {
            throw new IllegalArgumentException(message + " requires an api url but is missing");
        }
    }

    private void isGitUrlPresent(String message) {
        if (isEmpty(gitBaseUrl)) {
            throw new IllegalArgumentException(message + " requires a git base url but is missing");
        }
    }

    private void isProjectIdPresent(String projectId, String message) {
        if (isEmpty(projectId)) {
            throw new IllegalArgumentException(message + " requires a projectId but is missing");
        }
    }

    private void isCredentialsPresent(String message) {
        // If credential is present then it should be StandardUsernamePasswordCredentials otherwise it should have
        // exploded in constructor
        // If not present it's weird as it is clearly not anonymous call so showstopper
        if (!credentials.isPresent()) {
            throw new IllegalArgumentException(message + " requires a valid api url but is missing");
        }
    }
}
