package org.jenkinsci.plugins.tuleap_git_branch_source.webhook.model;

public class WebHookRepresentation {
    private String tuleapProjectId;
    private String repositoryName;
    private String branchName;
    private String token;

    public String getTuleapProjectId() {
        return tuleapProjectId;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getToken() {
        return token;
    }
}
