package org.jenkinsci.plugins.tuleap_git_branch_source.client.api;

public class TuleapGitBranch {

    private final String name;
    private final String sha1;

    public TuleapGitBranch(String name, String sha1) {
        this.name = name;
        this.sha1 = sha1;
    }

    public String getName() {
        return name;
    }

    public String getSha1() {
        return sha1;
    }
}
