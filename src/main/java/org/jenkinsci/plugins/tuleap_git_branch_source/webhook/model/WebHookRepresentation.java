package org.jenkinsci.plugins.tuleap_git_branch_source.webhook.model;

public class WebHookRepresentation {
    private String tuleapProjectName;
    private String repositoryName;
    private String branchName;

    public String getTuleapProjectName() {
        return tuleapProjectName;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getBranchName() {
        return branchName;
    }

}
