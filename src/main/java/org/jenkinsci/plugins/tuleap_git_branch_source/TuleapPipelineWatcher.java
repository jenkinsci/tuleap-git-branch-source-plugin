package org.jenkinsci.plugins.tuleap_git_branch_source;

import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.model.listeners.SCMListener;
import hudson.scm.SCM;
import hudson.scm.SCMRevisionState;
import io.jenkins.plugins.tuleap_api.client.GitApi;
import io.jenkins.plugins.tuleap_api.client.TuleapApiGuiceModule;
import io.jenkins.plugins.tuleap_api.client.internals.entities.TuleapBuildStatus;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TuleapPipelineWatcher {
    private static final Logger LOGGER = Logger
        .getLogger(TuleapPipelineWatcher.class.getName());

    private static TuleapPipelineStatusNotifier getNotifier() {
        final Injector injector = Guice.createInjector(new TuleapApiGuiceModule());
        return new TuleapPipelineStatusNotifier(
            injector.getInstance(GitApi.class)
        );
    }

    @Extension
    public static class TuleapJobCheckOutListener extends SCMListener {
        @Override
        public void onCheckout(
            Run<?, ?> build,
            SCM scm,
            FilePath workspace,
            TaskListener listener,
            File changelogFile,
            SCMRevisionState pollingBaseline
        ) {
            LOGGER.log(Level.INFO, String.format("Tuleap build: Checkout > %s", build.getFullDisplayName()));
            getNotifier().sendBuildStatusToTuleap(build, listener.getLogger(), TuleapBuildStatus.pending);
        }
    }

    @Extension
    public static class TuleapJobCompletedListener extends RunListener<Run<?, ?>> {
        @Override
        public void onCompleted(Run<?, ?> build, @NonNull TaskListener listener) {
            LOGGER.log(Level.INFO, String.format("Tuleap build: Complete > %s", build.getFullDisplayName()));

            final Result buildResult = build.getResult();
            final TuleapBuildStatus status = (buildResult == Result.SUCCESS) ? TuleapBuildStatus.success : TuleapBuildStatus.failure;

            getNotifier().sendBuildStatusToTuleap(build, listener.getLogger(), status);
        }
    }
}
