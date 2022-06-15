package org.jenkinsci.plugins.tuleap_git_branch_source;

import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.jenkins.plugins.tuleap_api.client.GitApi;
import io.jenkins.plugins.tuleap_api.client.TuleapApiGuiceModule;
import io.jenkins.plugins.tuleap_api.client.internals.entities.TuleapBuildStatus;
import org.jenkinsci.plugins.tuleap_git_branch_source.notify.TuleapPipelineStatusHandler;
import org.jenkinsci.plugins.tuleap_git_branch_source.notify.TuleapPipelineStatusNotifier;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TuleapPipelineWatcher {
    private static final Logger LOGGER = Logger
        .getLogger(TuleapPipelineWatcher.class.getName());

    private static TuleapPipelineStatusHandler getHandler() {
        final Injector injector = Guice.createInjector(new TuleapApiGuiceModule());
        return new TuleapPipelineStatusHandler(
            new TuleapPipelineStatusNotifier(
                injector.getInstance(GitApi.class)
            )
        );
    }

    @Extension
    public static class TuleapJobCompletedListener extends RunListener<Run<?, ?>> {

        @Override
        public void onStarted(Run<?, ?> build, @NonNull TaskListener listener) {
            LOGGER.log(Level.INFO, String.format("Tuleap build: Start > %s", build.getFullDisplayName()));
            getHandler().handleCommitNotification(build, listener.getLogger(), TuleapBuildStatus.pending);
        }

        @Override
        public void onCompleted(Run<?, ?> build, @NonNull TaskListener listener) {
            LOGGER.log(Level.INFO, String.format("Tuleap build: Complete > %s", build.getFullDisplayName()));

            final Result buildResult = build.getResult();
            final TuleapBuildStatus status = (buildResult == Result.SUCCESS) ? TuleapBuildStatus.success : TuleapBuildStatus.failure;

            getHandler().handleCommitNotification(build, listener.getLogger(), status);
        }
    }
}
