package org.jenkinsci.plugins.tuleap_branch_source.client.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;


import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jenkinsci.plugins.tuleap_branch_source.client.TuleapClient;
import org.jenkinsci.plugins.tuleap_branch_source.client.api.TuleapApi;
import org.jenkinsci.plugins.tuleap_branch_source.client.api.TuleapGitBranch;
import org.jenkinsci.plugins.tuleap_branch_source.client.api.TuleapGitRepository;
import org.jenkinsci.plugins.tuleap_branch_source.client.api.TuleapProject;
import org.jenkinsci.plugins.tuleap_branch_source.client.api.TuleapProjectRepositories;
import org.jenkinsci.plugins.tuleap_branch_source.client.api.TuleapUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.apache.commons.lang3.StringUtils.endsWith;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.startsWith;

import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * A default implementation of a Tuleap Client
 */
class DefaultClient implements TuleapClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultClient.class);
    private final String apiBaseUrl, gitBaseUrl;
    private final OkHttpClient client;
    private Optional<StandardCredentials> credentials;

    DefaultClient(Optional<StandardCredentials> credentials, final String apiBaseUrl, final String gitBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
        this.gitBaseUrl = gitBaseUrl;
        this.credentials = credentials;
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
                TuleapApi api = parse(body.string(), TuleapApi.class);
                return "1".equals(api.getApiVersion());
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
        String projectsApiUrl = apiBaseUrl + TULEAP_API_PROJECT_PATH ;
        //If property is_member_of is not defined, api will respond with all projects in read-only mode
        if (isMemberOf) {
            final String urlEncodedQueryObject = URLEncoder.encode(IS_MEMBER_OF_QUERY_OBJECT_PATTERN, StandardCharsets.UTF_8.displayName());
            projectsApiUrl += QUERY_OBJECT_PARAM + urlEncodedQueryObject;
        }
        Request req = new Request.Builder()
            .url(projectsApiUrl)
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
                return Stream.of(parse(body.string(), TuleapProject[].class));
            }
            return Stream.empty();
        } catch (IOException e) {
            throw new IOException("Retrieve current user's projects encounter error", e);
        }
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

    /**
     * {@inheritDoc}
     */
    public final Stream<TuleapGitBranch> branchByGitRepo(String gitRepoPath, String projectName)
        throws IOException, NoSingleRepoByPathException {
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
            return new LsRemoteCommand(null)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                .setRemote(remote)
                .setHeads(true)
                .call()
                .stream()
                .map(refToOFGitBranch());
        } catch (GitAPIException e) {
            throw new TuleapGitException(gitBaseUrl, gitRepoPath, e);
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
        return ref -> new TuleapGitBranch(ref.getName(), ref.getObjectId().getName());
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
