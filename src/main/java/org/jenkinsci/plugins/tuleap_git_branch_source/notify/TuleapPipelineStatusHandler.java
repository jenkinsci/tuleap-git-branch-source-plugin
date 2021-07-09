package org.jenkinsci.plugins.tuleap_git_branch_source.notify;

import hudson.model.Run;
import io.jenkins.plugins.tuleap_api.client.internals.entities.TuleapBuildStatus;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapSCMSource;
import org.jenkinsci.plugins.tuleap_git_branch_source.trait.TuleapCommitNotificationTrait;

import java.io.PrintStream;

public class TuleapPipelineStatusHandler {

    private final TuleapPipelineStatusNotifier notifier;

    public TuleapPipelineStatusHandler(TuleapPipelineStatusNotifier notifier) {
        this.notifier = notifier;
    }

    public void handleCommitNotification(Run<?, ?> build, PrintStream logger, TuleapBuildStatus status) {
        TuleapSCMSource source = this.getTuleapSCMSource(build);

        if (source == null) {
            return;
        }

        if (!this.isCommitNotificationTraitsEnabled(source)) {
            return;
        }

        logger.println("Sending the commit build status");
        this.notifier.sendBuildStatusToTuleap(build, logger, status, source);
    }

    private TuleapSCMSource getTuleapSCMSource(Run<?, ?> build) {
        final SCMSource source = SCMSource.SourceByItem.findSource(build.getParent());
        if (!(source instanceof TuleapSCMSource)) {
            return null;
        }
        return (TuleapSCMSource) source;
    }

    private boolean isCommitNotificationTraitsEnabled(TuleapSCMSource source) {
        return source.getTraits().stream().anyMatch(scmSourceTrait ->
            scmSourceTrait instanceof TuleapCommitNotificationTrait);
    }

}
