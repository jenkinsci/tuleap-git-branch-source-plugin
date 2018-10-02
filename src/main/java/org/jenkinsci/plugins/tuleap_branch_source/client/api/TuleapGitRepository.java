package org.jenkinsci.plugins.tuleap_branch_source.client.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Encapsulate the Tuleap git representation and JGit git repo Translation of Tuleap's GitRepositoryRepresentation
 *
 * @see <a href="https://tuleap.net/api/explorer/#!/git/retrieve">https://tuleap.net/api/explorer/#!/git/retrieve</a>
 * @see <a href="https://tuleap.net/api/explorer/#!/projects/retrieveGit">https://tuleap.net/api/explorer/#!/projects/retrieveGit</a>
 * @see <a href="https://tuleap.net/api/explorer/#!/git/retrieveRepositoryResource">GitRepositoryRepresentation</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TuleapGitRepository {

    private int id;
    private String uri, name, path, description;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
