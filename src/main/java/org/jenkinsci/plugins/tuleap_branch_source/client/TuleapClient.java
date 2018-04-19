package org.jenkinsci.plugins.tuleap_branch_source.client;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


import org.eclipse.jgit.api.errors.TransportException;
import org.jenkinsci.plugins.tuleap_branch_source.client.api.TuleapGitBranch;
import org.jenkinsci.plugins.tuleap_branch_source.client.api.TuleapGitRepository;
import org.jenkinsci.plugins.tuleap_branch_source.client.api.TuleapProject;
import org.jenkinsci.plugins.tuleap_branch_source.client.api.TuleapUser;

/**
 * Defines a Tuleap REST client and its behaviours given some configurations.
 * Translates Tuleap api response from api php object to api java object and populate these objects to be used by the
 * SCM api.
 *
 * source https://www.forge.orange-labs.fr/api/explorer/resources.json and it represent the v1
 * @see <a href= https://www.forge.orange-labs.fr/api/explorer/>API OrangeForge</a>
 */
public interface TuleapClient {

    String DEFAULT_TULEAP_DOMAIN_URL = "https://www.forge.orange-labs.fr";

    /**
     * Git URL as default configured in /etc/tuleap/plugins/git/etc/config.inc
     * https://tuleap.net/pipermail/tuleap-devel/2015-December/004425.html
     * http://tuleap-documentation.readthedocs.io/en/latest/installation-guide/advanced-configuration.html#tuleap-configuration
    */
    String DEFAULT_GIT_HTTPS_PATH = "/plugins/git/";

    /**
     * The api path according to api v1
     */
    String DEFAULT_TULEAP_API_PATH = "/api";

    /**
     * The api explorer path according to api v1
     */
    //API has changed no resources.json anymore :(
    String TULEAP_API_EXPLORER_PATH = "/explorer/swagger.json";

    /**
     * The projects api path according to api v1
     */
    String TULEAP_API_PROJECT_PATH = "/projects";

    /**
     * The users api path according to api v1
     */
    String TULEAP_API_USER_PATH = "/users";

    /**
     * The git api path according to api v1
     */
    String TULEAP_API_GIT_PATH = "/git";

    /**
     * The query param syntax according to api v1
     */
    String QUERY_OBJECT_PARAM = "?query=";

    /**
     * The query object to fetch users by username according to api v1
     */
    String BY_USERNAME_QUERY_OBJECT_PATTERN = "{\"username\":\"%s\"}";

    /**
     * The query object to fetch projects current user is member of according to api v1
     */
    String IS_MEMBER_OF_QUERY_OBJECT_PATTERN = "{\"is_member_of\":true}";

    /**
     * A means to tell we did a successful http call to a Tuleap server with correct registered credentials
     * From Tuleap docs : Note that when accessing this route without authentication certain properties will not be
     * returned in the response.
     * @return true(false) if http call is (un)successful and (does not )contains appropriate user data i.e. email +
     * status
     */
    boolean isCredentialValid() throws IOException;

    /**
     * A means to tell we did a successful http call to a Tuleap server
     * From Tuleap docs : Note that when accessing this route without authentication certain properties will not be
     * returned in the response.
     * @return true(false) if http call is (un)successful and (does not )contains appropriate user data
     */
    boolean isServerUrlValid() throws IOException ;

    /**
     * Return projects current user has access. Using param object ?query={"is_member_of": true} of OrangeForge api
     * it returns only projects user is member of
     * From docs
     * https://www.forge.orange-labs.fr/api/explorer/#!/projects/retrieve : Please note that {"is_member_of": false} is
     * not supported and will result in a 400 Bad Request error.
     *
     * @param isMemberOf if return only projects user is member of
     * @return list of projects current user has access
     * @throws IOException in case HTTP errors occurs or parsing of response fail
     */
    Stream<TuleapProject> allUserProjects(boolean isMemberOf) throws IOException ;

    /**
     * Get a list of git repositories of the project identified by a projectIf
     *
     * @param projectId the id of project to inspect
     * @return the list of repositories
     * @throws IOException in case HTTP errors occurs or parsing of response fail
     */
    Stream<TuleapGitRepository> allProjectRepositories(final String projectId) throws IOException;

    /**
     * Get the project identified by the projectId
     *
     * @param projectId the id of the requested project
     * @return An optional wrapper of the project
     * @throws IOException in case HTTP errors occurs or parsing of response fail
     */
    Optional<TuleapProject> projectById(final String projectId) throws IOException;

    /**
     * Get all head refs of a git repository define by its path
     *
     * @param gitRepoPath the  git repo path to inspect
     * @param projectName the  project name corresponding to the git repo path to inspect
     * @return all head refs
     * @throws IOException in case git connexion pbm
     * @throws NoSingleRepoByPathException in case multiple git repo are represented by a path, this is blocking
     */
    Stream<TuleapGitBranch> branchByGitRepo(String gitRepoPath, String projectName)
        throws IOException, NoSingleRepoByPathException;

    /**
     * If multpile git repo are represented by a path it is a show stopper as in Tuleap the discriminant is the git
     * repo path
     */
    class NoSingleRepoByPathException extends RuntimeException {

        public NoSingleRepoByPathException(final String path, final String doublonUri, final String anotherDoublonUri) {
            super("Multiple repository with path '" + path + "' :" + doublonUri + " and " + anotherDoublonUri);
        }
    }

    /**
     * Multiple user represented by a given username passxord credentials is clearly a showstopper
     */
    class MultipleUserMatchingCredentialsException extends RuntimeException {

        public MultipleUserMatchingCredentialsException(List<TuleapUser> users) {
            super("Some username"+ users.stream().map(TuleapUser::getEmail).reduce("", (x, y) -> x+y));
        }
    }

    /**
     * If some git errors occurs, it is not a showstopper though as it may be just temporary. No retry mechanism is yet
     * implemented
     */
    class TuleapGitException extends IOException {
        public TuleapGitException(final String uri, final String path, Throwable t) {
            super("Unable to communicate to Tuleap git at " + uri + "/" + path, t);
        }

        public TuleapGitException(String remote, TransportException e) {
            super("Unable to communicate to Tuleap git at " + remote, e);
        }
    }
}
