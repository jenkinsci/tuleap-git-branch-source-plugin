package org.jenkinsci.plugins.tuleap_git_branch_source.stubs;

import io.jenkins.plugins.tuleap_api.client.GitHead;
import io.jenkins.plugins.tuleap_api.client.GitPullRequest;
import io.jenkins.plugins.tuleap_api.client.GitRepositoryReference;

public class GitPullRequestStub implements GitPullRequest {

    private final String id;

    private GitPullRequestStub(String id) {
        this.id = id;
    }

    public static GitPullRequestStub withId(String id) {
        return new GitPullRequestStub(id);
    }


    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public GitRepositoryReference getSourceRepository() {
        return null;
    }

    @Override
    public GitRepositoryReference getDestinationRepository() {
        return null;
    }

    @Override
    public String getSourceBranch() {
        return null;
    }

    @Override
    public String getDestinationBranch() {
        return null;
    }

    @Override
    public GitHead getHead() {
        return null;
    }
}
