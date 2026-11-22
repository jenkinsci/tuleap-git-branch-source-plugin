package org.jenkinsci.plugins.tuleap_git_branch_source.config;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Item;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import io.jenkins.plugins.tuleap_api.client.GitApi;
import io.jenkins.plugins.tuleap_credentials.TuleapAccessToken;
import jenkins.scm.api.*;
import org.jenkinsci.plugins.tuleap_git_branch_source.*;
import org.jenkinsci.plugins.tuleap_git_branch_source.helpers.TuleapApiRetriever;
import org.jetbrains.annotations.NotNull;

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
    public long lastModified() {
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
        public SCMFileSystem build(@NotNull Item owner, @NotNull SCM scm, SCMRevision rev) {
            return null;
        }

        @Override
        public SCMFileSystem build(@NonNull SCMSource source, @NonNull SCMHead head,
                                   @CheckForNull SCMRevision rev) {
            GitApi gitApi = TuleapApiRetriever.getGitApi();
            TuleapSCMSource tuleapSCMSource = (TuleapSCMSource) source;
            TuleapAccessToken tuleapAccessToken = this.getAccessKey(tuleapSCMSource);

            String ref;
            String repositoryId;
            if ((head instanceof TuleapBranchSCMHead)) {
                ref = head.getName();
                repositoryId = Integer.toString(tuleapSCMSource.getTuleapGitRepository().getId());
            } else if (head instanceof TuleapPullRequestSCMHead tlpHead) {
                if (rev instanceof TuleapPullRequestRevision) {
                    ref = ((TuleapPullRequestSCMHead) head).getOriginName();
                    repositoryId = Integer.toString(((TuleapPullRequestSCMHead) head).getOriginRepositoryId());
                } else {
                    ref = tlpHead.getTarget().getName();
                    repositoryId = Integer.toString(tlpHead.getTargetRepositoryId());
                }
            } else {
                return null;
            }

            return new TuleapSCMFileSystem(gitApi, repositoryId , ref, tuleapAccessToken, rev);
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
