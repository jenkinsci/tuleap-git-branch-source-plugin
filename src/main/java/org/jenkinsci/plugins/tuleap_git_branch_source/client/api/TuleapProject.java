package org.jenkinsci.plugins.tuleap_git_branch_source.client.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Translate a ProjectRepresentation
 *
 * @see <a href= https://tuleap.net/api/explorer/#!/projects/retrieve>https://tuleap.net/api/explorer/#!/projects/retrieve</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TuleapProject {

    private int id;
    private String shortname;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShortname() {
        return shortname;
    }
}
