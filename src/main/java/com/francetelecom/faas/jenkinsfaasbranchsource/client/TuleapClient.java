package com.francetelecom.faas.jenkinsfaasbranchsource.client;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;


import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapGitBranch;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapGitRepository;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapProject;

/**
 * Defines a Tuleap client behaviours
 */
public interface TuleapClient {

    String TULEAP_API_PROJECT_PATH = "/projects";

    String TULEAP_API_USER_PATH = "/users";

    String TULEAP_API_GIT_PATH = "/git";

    /**
     * A means to tell we http call a Tuleap server with correct registered credentials
     *
     * @return true(false) if http call is (un)successful
     */
    boolean isCredentialValid() throws IOException;

    List<TuleapProject> allUserProjects() throws IOException;

    List<TuleapGitRepository> allProjectRepositories(final String projectId) throws IOException;

    TuleapProject projectById(final String projectId) throws IOException;

    List<TuleapGitBranch> branchByGitRepo(String gitRepoPath)
        throws IOException, NoSingleRepoByPathException, NoSuchElementException;

    class NoSingleRepoByPathException extends RuntimeException {

        public NoSingleRepoByPathException(final String path, final String doublonUri, final String anotherDoublonUri) {
            super("Multiple repository with path '" + path + "' :" + doublonUri + " and " + anotherDoublonUri);
        }
    }

    class TuleapGitException extends RuntimeException {
        public TuleapGitException(final String uri, final String path, Throwable t) {
            super("Unable to communicate to OrangeForge git at " + uri + "/" + path, t);
        }
    }
}
