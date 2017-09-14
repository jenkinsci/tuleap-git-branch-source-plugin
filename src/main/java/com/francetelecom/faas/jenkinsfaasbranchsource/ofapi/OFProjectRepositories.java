package com.francetelecom.faas.jenkinsfaasbranchsource.ofapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Translation of OrangeForge Project's repositories
 *  @see <a href=https://www.forge.orange-labs.fr/api/explorer/#!/projects/retrieveGit>https://www.forge.orange-labs.fr/api/explorer/#!/projects/retrieveGit</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OFProjectRepositories {

	private List<OFGitRepository> repositories;

	public List<OFGitRepository> getRepositories() {
		return Collections.unmodifiableList(repositories);
	}

	public void setRepositories(List<OFGitRepository> repositories) {
		this.repositories = new ArrayList<>(repositories == null ? Collections.<OFGitRepository>emptyList() : repositories);
	}
}
