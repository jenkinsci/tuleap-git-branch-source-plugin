package org.jenkinsci.plugins.tuleap_git_branch_source.notify;

import hudson.model.Run;
import hudson.plugins.git.util.BuildData;
import io.jenkins.plugins.tuleap_api.client.GitApi;
import io.jenkins.plugins.tuleap_api.client.internals.entities.TuleapBuildStatus;
import io.jenkins.plugins.tuleap_credentials.TuleapAccessToken;

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

        final BuildData gitData = build.getAction(BuildData.class);
        if (gitData == null) {
            throw new RuntimeException(
                "Failed to retrieve Git Data. Please check the configuration."
            );
        }

        final int repository_id = source.getTuleapGitRepository().getId();
        logger.printf(
            "Notifying Tuleap about build status: %s",
            status.toString()
        );

        this.gitApi.sendBuildStatus(
            Integer.toString(repository_id),
            gitData.lastBuild.getSHA1().name(),
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
}
