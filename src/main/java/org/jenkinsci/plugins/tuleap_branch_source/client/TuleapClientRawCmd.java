package org.jenkinsci.plugins.tuleap_branch_source.client;

import org.jenkinsci.plugins.tuleap_branch_source.client.api.*;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

public class TuleapClientRawCmd {

    protected TuleapClient client;

    public void setClient(TuleapClient client) {
        this.client = client;
    }

    @FunctionalInterface
    public interface Command<T> {
        T call() throws IOException;
    }

    public static class AllUserProjects extends TuleapClientRawCmd implements Command<Stream<TuleapProject>> {

        private final boolean fetchProjectsUserIsMemberOf;

        public AllUserProjects(boolean fetchProjectsUserIsMemberOf) {
            this.fetchProjectsUserIsMemberOf = fetchProjectsUserIsMemberOf;
        }

        @Override
        public Stream<TuleapProject> call() throws IOException {
            return client.allUserProjects(fetchProjectsUserIsMemberOf);
        }
    }

    public static class AllRepositoriesByProject extends TuleapClientRawCmd implements Command<Stream<TuleapGitRepository>> {

        private final String projectId;

        public AllRepositoriesByProject(final String projectId) {
            this.projectId = projectId;
        }

        @Override
        public Stream<TuleapGitRepository> call() throws IOException {
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

    public static class AllBranchesByGitRepo extends TuleapClientRawCmd implements Command<Stream<TuleapGitBranch>> {

        private final String gitRepoPath, projectName;

        public AllBranchesByGitRepo(String gitRepoPath, String projectName) {
            this.gitRepoPath = gitRepoPath;
            this.projectName = projectName;
        }

        @Override
        public Stream<TuleapGitBranch> call() throws IOException {
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

    public static class Branches extends TuleapClientRawCmd implements Command<Stream<TuleapBranches>> {
        private final int idRepo;

        public Branches(int idRepo) {
            this.idRepo = idRepo;
        }

        @Override
        public Stream<TuleapBranches> call() throws IOException {
            return client.allBranches(idRepo);
        }
    }

    public static class GetJenkinsFile extends TuleapClientRawCmd implements Command<Optional<TuleapFileContent>> {

        private int idRepo;
        private String pathToFile;
        private String ref;

        public GetJenkinsFile(int idRepo, String pathToFile, String ref) {
            this.idRepo = idRepo;
            this.ref = ref;
            this.pathToFile = pathToFile;
        }

        @Override
        public Optional<TuleapFileContent> call() throws IOException {
            return client.getJenkinsFile(idRepo, pathToFile, ref);
        }
    }
}
