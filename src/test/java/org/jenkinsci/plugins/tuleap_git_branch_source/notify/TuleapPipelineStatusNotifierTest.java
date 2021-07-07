package org.jenkinsci.plugins.tuleap_git_branch_source.notify;

import hudson.plugins.git.util.Build;
import hudson.plugins.git.util.BuildData;
import io.jenkins.plugins.tuleap_api.client.GitApi;
import io.jenkins.plugins.tuleap_api.client.internals.entities.TuleapBuildStatus;
import io.jenkins.plugins.tuleap_api.deprecated_client.api.TuleapGitRepository;
import io.jenkins.plugins.tuleap_credentials.TuleapAccessToken;
import jenkins.scm.api.SCMSource;
import org.eclipse.jgit.lib.ObjectId;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapSCMSource;
import org.jenkinsci.plugins.tuleap_git_branch_source.config.TuleapConnector;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.PrintStream;

import static org.mockito.Mockito.*;

public class TuleapPipelineStatusNotifierTest {
    private GitApi gitApi;
    private TuleapPipelineStatusNotifier notifier;
    private TuleapAccessToken accessKey;
    private MockedStatic<SCMSource.SourceByItem> sourceByItem;
    private MockedStatic<TuleapConnector> tuleapConnector;

    @Before
    public void setUp() {
        this.gitApi = mock(GitApi.class);
        this.accessKey = mock(TuleapAccessToken.class);
        this.sourceByItem = Mockito.mockStatic(SCMSource.SourceByItem.class);
        this.tuleapConnector = Mockito.mockStatic(TuleapConnector.class);

        this.notifier = new TuleapPipelineStatusNotifier(this.gitApi);
    }

    @After
    public void tearDown() {
        this.sourceByItem.close();
        this.tuleapConnector.close();
    }

    @Test(expected = RuntimeException.class)
    public void testItThrowsAnExceptionWhenAccessKeyNotFound() {
        final WorkflowRun build = mock(WorkflowRun.class);
        final TuleapSCMSource source = mock(TuleapSCMSource.class);
        final PrintStream logger = mock(PrintStream.class);
        final WorkflowJob workflowJob = mock(WorkflowJob.class);

        when(build.getParent()).thenReturn(workflowJob);

        this.sourceByItem.when(() -> SCMSource.SourceByItem.findSource(workflowJob)).thenReturn(source);
        this.tuleapConnector.when(() -> TuleapConnector.lookupScanCredentials(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(null);

        verify(this.gitApi, never()).sendBuildStatus(
            "5",
            "aeiouy123456",
            TuleapBuildStatus.success,
            this.accessKey
        );

        this.notifier.sendBuildStatusToTuleap(build, logger, TuleapBuildStatus.success, source);
    }

    @Test(expected = RuntimeException.class)
    public void testItThrowsAnExceptionWhenGitDataNotFound() {
        final WorkflowRun build = mock(WorkflowRun.class);
        final TuleapSCMSource source = mock(TuleapSCMSource.class);
        final PrintStream logger = mock(PrintStream.class);
        final WorkflowJob workflowJob = mock(WorkflowJob.class);
        final TuleapAccessToken accessKey = mock(TuleapAccessToken.class);

        when(build.getParent()).thenReturn(workflowJob);
        when(build.getAction(BuildData.class)).thenReturn(null);

        this.sourceByItem.when(() -> SCMSource.SourceByItem.findSource(workflowJob)).thenReturn(source);
        this.tuleapConnector.when(() -> TuleapConnector.lookupScanCredentials(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(accessKey);

        verify(this.gitApi, never()).sendBuildStatus(
            "5",
            "aeiouy123456",
            TuleapBuildStatus.success,
            this.accessKey
        );

        this.notifier.sendBuildStatusToTuleap(build, logger, TuleapBuildStatus.success, source);
    }

    @Test
    public void testItNotifiesTuleap() {
        final WorkflowRun build = mock(WorkflowRun.class);
        final TuleapSCMSource source = mock(TuleapSCMSource.class);
        final PrintStream logger = mock(PrintStream.class);
        final WorkflowJob workflowJob = mock(WorkflowJob.class);
        final TuleapAccessToken accessKey = mock(TuleapAccessToken.class);
        final BuildData gitData = mock(BuildData.class);
        final ObjectId sha1 = mock(ObjectId.class);
        final TuleapGitRepository repository = new TuleapGitRepository();
        final Build lastBuild = mock(Build.class);

        repository.setId(5);
        gitData.lastBuild = lastBuild;

        when(build.getParent()).thenReturn(workflowJob);
        when(build.getAction(BuildData.class)).thenReturn(gitData);
        when(lastBuild.getSHA1()).thenReturn(sha1);
        when(sha1.name()).thenReturn("aeiouy123465");
        when(source.getTuleapGitRepository()).thenReturn(repository);

        this.sourceByItem.when(() -> SCMSource.SourceByItem.findSource(workflowJob)).thenReturn(source);
        this.tuleapConnector.when(() -> TuleapConnector.lookupScanCredentials(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(accessKey);

        verify(this.gitApi, atMostOnce()).sendBuildStatus(
            "5",
            "aeiouy123456",
            TuleapBuildStatus.success,
            accessKey
        );

        this.notifier.sendBuildStatusToTuleap(build, logger, TuleapBuildStatus.success, source);
    }
}
