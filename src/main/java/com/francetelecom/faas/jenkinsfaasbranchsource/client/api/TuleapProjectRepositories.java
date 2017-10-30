package com.francetelecom.faas.jenkinsfaasbranchsource.client.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Translation of OrangeForge Project's repositories
 *
 * @see <a href= https://www.forge.orange-labs.fr/api/explorer/#!/projects/retrieveGit>https://www.forge.orange-labs.fr/api/explorer/#!/projects/retrieveGit</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TuleapProjectRepositories {

    private List<TuleapGitRepository> repositories;

    public List<TuleapGitRepository> getRepositories() {
        return Collections.unmodifiableList(repositories);
    }

    public void setRepositories(List<TuleapGitRepository> repositories) {
        this.repositories = new ArrayList<>(repositories == null ? Collections.emptyList() : repositories);
    }
}
