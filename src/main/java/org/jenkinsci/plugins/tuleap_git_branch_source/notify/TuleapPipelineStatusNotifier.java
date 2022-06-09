package org.jenkinsci.plugins.tuleap_git_branch_source.notify;

import hudson.model.Run;
import io.jenkins.plugins.tuleap_api.client.GitApi;
import io.jenkins.plugins.tuleap_api.client.internals.entities.TuleapBuildStatus;
import io.jenkins.plugins.tuleap_credentials.TuleapAccessToken;

import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMRevisionAction;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapBranchSCMRevision;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapPullRequestRevision;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapSCMSource;
import org.jenkinsci.plugins.tuleap_git_branch_source.config.TuleapConnector;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;

public class TuleapPipelineStatusNotifier {
    private final GitApi gitApi;

    public TuleapPipelineStatusNotifier(GitApi gitApi) {
        this.gitApi = gitApi;
    }

    public void sendBuildStatusToTuleap(Run<?, ?> build, PrintStream logger, TuleapBuildStatus status, TuleapSCMSource source) {
        final TuleapAccessToken token = getAccessKey(source);
        if (token == null) {
            throw new RuntimeException(
                "Access key for project not found. Please check your project configuration."
            );
        }

        String hash = this.getRevisionHash(source, build);

        final int repository_id = source.getTuleapGitRepository().getId();
        logger.printf(
            "Notifying Tuleap about build status: %s",
            status.toString()
        );

        this.gitApi.sendBuildStatus(
            Integer.toString(repository_id),
            hash,
            status,
            token
        );
    }

    @Nullable
    private TuleapAccessToken getAccessKey(TuleapSCMSource source) {
        return TuleapConnector.lookupScanCredentials(
            source.getOwner(),
            source.getApiBaseUri(),
            source.getCredentialsId()
        );
    }

    private String getRevisionHash(TuleapSCMSource source, Run<?,?> build){
        SCMRevision revision = SCMRevisionAction.getRevision(source, build);
        String hash;
        if (revision instanceof TuleapBranchSCMRevision) {
            hash = ((TuleapBranchSCMRevision) revision).getHash();
        } else if (revision instanceof TuleapPullRequestRevision) {
            TuleapBranchSCMRevision origin = (TuleapBranchSCMRevision) ((TuleapPullRequestRevision) revision).getOrigin();
            hash = origin.getHash();
        } else {
            throw new InvalidRetrievedRevisionType();
        }
        return hash;
    }
}
