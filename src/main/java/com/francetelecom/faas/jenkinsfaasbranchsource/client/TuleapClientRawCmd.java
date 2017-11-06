package com.francetelecom.faas.jenkinsfaasbranchsource.client;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapGitBranch;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapGitRepository;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapProject;

public class TuleapClientRawCmd {

    protected TuleapClient client;

    public void setClient(TuleapClient client) {
        this.client = client;
    }

    @FunctionalInterface
    public interface Command<T> {
        T call() throws IOException;
    }

    public static class AllUserProjects extends TuleapClientRawCmd implements Command<List<TuleapProject>> {

        private final boolean fetchProjectsUserIsMemberOf;

        public AllUserProjects(boolean fetchProjectsUserIsMemberOf) {
            this.fetchProjectsUserIsMemberOf = fetchProjectsUserIsMemberOf;
        }

        @Override
        public List<TuleapProject> call() throws IOException {
            return client.allUserProjects(fetchProjectsUserIsMemberOf);
        }
    }

    public static class AllRepositoriesByProject extends TuleapClientRawCmd implements Command<List<TuleapGitRepository>> {

        private final String projectId;

        public AllRepositoriesByProject(final String projectId) {
            this.projectId = projectId;
        }

        @Override
        public List<TuleapGitRepository> call() throws IOException {
            return client.allProjectRepositories(projectId);
        }
    }

    public static class ProjectById extends TuleapClientRawCmd implements Command<Optional<TuleapProject>> {

        private final String projectId;

        public ProjectById(String projectId) {
            this.projectId = projectId;
        }

        @Override
        public Optional<TuleapProject> call() throws IOException {
            return client.projectById(projectId);
        }
    }

    public static class AllBranchesByGitRepo extends TuleapClientRawCmd implements Command<List<TuleapGitBranch>> {

        private final String gitRepoPath, projectName;

        public AllBranchesByGitRepo(String gitRepoPath, String projectName) {
            this.gitRepoPath = gitRepoPath;
            this.projectName = projectName;
        }

        @Override
        public List<TuleapGitBranch> call() throws IOException {
            return client.branchByGitRepo(gitRepoPath, projectName);
        }
    }

    public static class IsTuleapServerUrlValid extends TuleapClientRawCmd implements Command<Boolean> {

        @Override
        public Boolean call() throws IOException {
            return client.isServerUrlValid();
        }
    }

    public static class IsCredentialsValid extends TuleapClientRawCmd implements Command<Boolean> {

        @Override
        public Boolean call() throws IOException {
            return client.isCredentialValid();
        }
    }
}
