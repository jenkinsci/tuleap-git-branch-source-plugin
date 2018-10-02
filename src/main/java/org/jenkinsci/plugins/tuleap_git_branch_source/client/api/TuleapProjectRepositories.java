package org.jenkinsci.plugins.tuleap_git_branch_source.client.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Translation of Tuleap Project's repositories
 *
 * @see <a href= https://tuleap.net/api/explorer/#!/projects/retrieveGit>https://tuleap.net/api/explorer/#!/projects/retrieveGit</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TuleapProjectRepositories {

    private List<TuleapGitRepository> repositories = new ArrayList<>();

    public List<TuleapGitRepository> getRepositories() {
        return Collections.unmodifiableList(repositories);
    }

    public void setRepositories(List<TuleapGitRepository> repositories) {
        this.repositories = new ArrayList<>(repositories == null ? Collections.emptyList() : repositories);
    }
}
