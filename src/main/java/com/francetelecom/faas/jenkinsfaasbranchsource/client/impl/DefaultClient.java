package com.francetelecom.faas.jenkinsfaasbranchsource.client.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;


import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.TuleapClient;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapGitBranch;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapGitRepository;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapProject;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapProjectRepositories;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapUser;
import com.francetelecom.faas.jenkinsfaasbranchsource.config.BasicAuthInterceptor;

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

    public DefaultClient(Optional<StandardCredentials> credentials, final String apiBaseUrl, final String gitBaseUrl) {
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
        // If credential is present then it should be StandardUsernamePasswordCredentials otherwise it should have
        // exploded in constructor
        // If not present it's weird as it is clearly not anonymous call so showstopper
        if (!credentials.isPresent()) {
            throw new IllegalArgumentException("Want to check credential though no credentials info provided ..weird");
        }
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
                    return StringUtils.isNotBlank(users.get(0).getEmail()) && StringUtils.isNotBlank(users.get(0)
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
        return !allUserProjects(false).isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public final List<TuleapProject> allUserProjects(boolean isMemberOf) throws IOException {
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
                return Arrays.asList(parse(body.string(), TuleapProject[].class));
            }
            return new ArrayList<>();
        } catch (IOException e) {
            throw new IOException("Retrieve current user's projects encounter error", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final Optional<TuleapProject> projectById(final String projectId) throws IOException {
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
    public final List<TuleapGitRepository> allProjectRepositories(final String projectId) throws IOException {
        return projectRepositoriesWrapper(projectId).getRepositories();
    }

    /**
     * Get git repositories wrapper per api contract.
     *
     * @param projectId the project id to inspect
     * @return the repositories wrapper {@see OFProjectRepositories}
     * @throws IOException in case HTTP errors occurs or parsing of response fail
     */
    private TuleapProjectRepositories projectRepositoriesWrapper(final String projectId) throws IOException {
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
    public final List<TuleapGitBranch> branchByGitRepo(String gitRepoPath)
        throws IOException, NoSingleRepoByPathException {
        // If not present it's weird as it is clearly not anonymous call so showstopper
        if (!credentials.isPresent()) {
            throw new IllegalArgumentException("Want to check credential though no credentials info provided ..weird");
        }
        try {
            LOGGER.info("Ls-remoting heads of git repository at {} + {}", gitBaseUrl, gitRepoPath);
            final String username = ((StandardUsernamePasswordCredentials)credentials.get()).getUsername();
            final String password = ((StandardUsernamePasswordCredentials)credentials.get()).getPassword().getPlainText();
            if (!StringUtils.startsWith(gitRepoPath, "faas/")) {
                gitRepoPath = "faas/" + gitRepoPath;
            }
            if (!StringUtils.endsWith(gitRepoPath, ".git")) {
                gitRepoPath = gitRepoPath + ".git";
            }
            final String remote = gitBaseUrl + gitRepoPath;
            return new LsRemoteCommand(null)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                .setRemote(remote)
                .setHeads(true)
                .call()
                .stream()
                .map(refToOFGitBranch())
                .collect(Collectors.toList());
        } catch (GitAPIException e) {
            throw new TuleapGitException(gitBaseUrl, gitRepoPath, e);
        }
    }

    private Optional<TuleapGitRepository> gitRepoByPath(final String projectId, final String gitRepoPath)
        throws IOException, NoSingleRepoByPathException {
        return allProjectRepositories(projectId)
            .stream()
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
}
