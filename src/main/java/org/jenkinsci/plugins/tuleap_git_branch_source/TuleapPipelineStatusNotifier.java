package org.jenkinsci.plugins.tuleap_git_branch_source;

import hudson.model.Run;
import hudson.plugins.git.util.BuildData;
import io.jenkins.plugins.tuleap_api.client.GitApi;
import io.jenkins.plugins.tuleap_api.client.internals.entities.TuleapBuildStatus;
import io.jenkins.plugins.tuleap_credentials.TuleapAccessToken;

import jenkins.scm.api.SCMSource;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;

public class TuleapPipelineStatusNotifier {
    private final GitApi gitApi;

    public TuleapPipelineStatusNotifier(GitApi gitApi) {
        this.gitApi = gitApi;
    }

    @Nullable
    private TuleapSCMSource getTuleapSCMSource(Run<?, ?> build) {
        final SCMSource source = SCMSource.SourceByItem.findSource(build.getParent());
        if (!(source instanceof TuleapSCMSource)) {
            return null;
        }
        return (TuleapSCMSource) source;
    }

    public void sendBuildStatusToTuleap(Run<?, ?> build, PrintStream logger, TuleapBuildStatus status) {
        final TuleapSCMSource source = getTuleapSCMSource(build);
        if (source == null) {
            // Not a TuleapSCMSource, abort
            return;
        }

        final TuleapAccessToken token = source.getCredentials();
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
}
