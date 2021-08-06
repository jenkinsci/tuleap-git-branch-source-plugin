package org.jenkinsci.plugins.tuleap_git_branch_source.config;

import com.google.inject.Guice;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Item;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import io.jenkins.plugins.tuleap_api.client.GitApi;
import io.jenkins.plugins.tuleap_api.client.TuleapApiGuiceModule;
import io.jenkins.plugins.tuleap_credentials.TuleapAccessToken;
import jenkins.scm.api.*;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapBranchSCMHead;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapPullRequestSCMHead;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapSCMSource;
import org.jenkinsci.plugins.tuleap_git_branch_source.helpers.TuleapApiRetriever;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class TuleapSCMFileSystem extends SCMFileSystem {

    private GitApi gitApi;
    private final String repositoryId;
    private final String commitReference;
    private TuleapAccessToken tuleapAccessToken;

    protected TuleapSCMFileSystem(GitApi gitApi, String repositoryId, String commitReference, TuleapAccessToken tuleapAccessToken, SCMRevision rev) {
        super(rev);
        this.gitApi = gitApi;
        this.repositoryId = repositoryId;
        this.commitReference = commitReference;
        this.tuleapAccessToken = tuleapAccessToken;
    }

    @Override
    public long lastModified() throws IOException, InterruptedException {
        return this.gitApi.getCommit(this.repositoryId, this.commitReference, this.tuleapAccessToken).getCommitDate().toInstant().toEpochMilli();
    }

    @NotNull
    @Override
    public SCMFile getRoot() {
        return new TuleapSCMFile(this.gitApi, this.repositoryId, this.commitReference, this.tuleapAccessToken);
    }

    @Extension
    public static class BuilderImpl extends Builder {

        @Override
        public boolean supports(SCM source) {
            return false;
        }

        @Override
        public boolean supports(SCMSource source) {
            return source instanceof TuleapSCMSource;
        }

        @Override
        protected boolean supportsDescriptor(SCMDescriptor descriptor) {
            return false;
        }

        @Override
        protected boolean supportsDescriptor(SCMSourceDescriptor descriptor) {
            return descriptor instanceof TuleapSCMSource.DescriptorImpl;
        }

        @Override
        public SCMFileSystem build(@NotNull Item owner, @NotNull SCM scm, SCMRevision rev) throws IOException, InterruptedException {
            return null;
        }

        @Override
        public SCMFileSystem build(@NonNull SCMSource source, @NonNull SCMHead head,
                                   @CheckForNull SCMRevision rev) {
            GitApi gitApi = TuleapApiRetriever.getGitApi();
            TuleapSCMSource tuleapSCMSource = (TuleapSCMSource) source;
            TuleapAccessToken tuleapAccessToken = this.getAccessKey(tuleapSCMSource);

            String ref;
            if ((head instanceof TuleapBranchSCMHead)) {
                ref = head.getName();
            } else if (head instanceof TuleapPullRequestSCMHead) {
                ref = ((TuleapPullRequestSCMHead) head).getOriginName();
            } else {
                return null;
            }
            return new TuleapSCMFileSystem(gitApi, Integer.toString(tuleapSCMSource.getTuleapGitRepository().getId()), ref, tuleapAccessToken, rev);
        }

        private TuleapAccessToken getAccessKey(TuleapSCMSource source) {
            return TuleapConnector.lookupScanCredentials(
                source.getOwner(),
                source.getApiBaseUri(),
                source.getCredentialsId()
            );
        }
    }
}
