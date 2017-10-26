package com.francetelecom.faas.jenkinsfaasbranchsource.client;


import java.io.IOException;
import java.util.List;


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

	public class AllUserProjects extends TuleapClientRawCmd implements Command<List<TuleapProject>> {

		@Override
		public List<TuleapProject> call() throws IOException {
			return client.allUserProjects();
		}
	}

	public class AllRepositoriesByProject extends TuleapClientRawCmd implements Command<List<TuleapGitRepository>> {

		private final String projectId;

		public AllRepositoriesByProject(final String projectId) {
			this.projectId=projectId;
		}

		@Override
		public List<TuleapGitRepository> call() throws IOException {
			return client.allProjectRepositories(projectId);
		}
	}

	public class ProjectById extends TuleapClientRawCmd implements Command<TuleapProject> {

		private final String projectId;

		public ProjectById(String projectId) {
			this.projectId = projectId;
		}

		@Override
		public TuleapProject call() throws IOException {
			return client.projectById(projectId);
		}
	}

	public class AllBranchesByGitRepo extends TuleapClientRawCmd implements Command<List<TuleapGitBranch>> {

		private final String gitRepoPath;

		public AllBranchesByGitRepo(String gitRepoPath) {
			this.gitRepoPath = gitRepoPath;
		}

		@Override
		public List<TuleapGitBranch> call() throws IOException {
			return client.branchByGitRepo(gitRepoPath);
		}
	}

	public class IsTuleapServerUrl extends TuleapClientRawCmd implements Command<Boolean> {

		@Override
		public Boolean call() throws IOException {
			//TODO isUrlValid no need cred
			return client.isCredentialValid();
		}
	}
}
