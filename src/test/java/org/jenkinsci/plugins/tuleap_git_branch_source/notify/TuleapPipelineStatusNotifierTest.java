package org.jenkinsci.plugins.tuleap_git_branch_source.notify;

import io.jenkins.plugins.tuleap_api.client.GitApi;
import io.jenkins.plugins.tuleap_api.client.GitHead;
import io.jenkins.plugins.tuleap_api.client.GitPullRequest;
import io.jenkins.plugins.tuleap_api.client.GitRepositoryReference;
import io.jenkins.plugins.tuleap_api.client.internals.entities.TuleapBuildStatus;
import io.jenkins.plugins.tuleap_api.deprecated_client.api.TuleapGitRepository;
import io.jenkins.plugins.tuleap_credentials.TuleapAccessToken;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.SCMRevisionAction;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.tuleap_git_branch_source.*;
import org.jenkinsci.plugins.tuleap_git_branch_source.config.TuleapConnector;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.io.PrintStream;

import static org.mockito.Mockito.*;

public class TuleapPipelineStatusNotifierTest {
    private GitApi gitApi;
    private TuleapPipelineStatusNotifier notifier;
    private TuleapAccessToken accessKey;
    private MockedStatic<SCMSource.SourceByItem> sourceByItem;
    private MockedStatic<TuleapConnector> tuleapConnector;
    private MockedStatic<SCMRevisionAction> scmRevisionAction;

    @Before
    public void setUp() {
        this.gitApi = mock(GitApi.class);
        this.accessKey = mock(TuleapAccessToken.class);
        this.sourceByItem = mockStatic(SCMSource.SourceByItem.class);
        this.tuleapConnector = mockStatic(TuleapConnector.class);
        this.scmRevisionAction = mockStatic(SCMRevisionAction.class);

        this.notifier = new TuleapPipelineStatusNotifier(this.gitApi);
    }

    @After
    public void tearDown() {
        this.sourceByItem.close();
        this.tuleapConnector.close();
        this.scmRevisionAction.close();
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
            any(),
            any(),
            any()
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

        this.sourceByItem.when(() -> SCMSource.SourceByItem.findSource(workflowJob)).thenReturn(source);
        this.tuleapConnector.when(() -> TuleapConnector.lookupScanCredentials(
            any(),
            any(),
            any()
        )).thenReturn(accessKey);

        verify(this.gitApi, never()).sendBuildStatus(
            "5",
            "aeiouy123456",
            TuleapBuildStatus.success,
            this.accessKey
        );

        this.notifier.sendBuildStatusToTuleap(build, logger, TuleapBuildStatus.success, source);
    }

    @Test(expected = RuntimeException.class)
    public void testItThrowsAnExceptionWhenTheHashIsNotFromTuleapRevisionSource() {
        final WorkflowRun build = mock(WorkflowRun.class);
        final TuleapSCMSource source = mock(TuleapSCMSource.class);
        final PrintStream logger = mock(PrintStream.class);
        final WorkflowJob workflowJob = mock(WorkflowJob.class);
        final TuleapAccessToken accessKey = mock(TuleapAccessToken.class);

        when(build.getParent()).thenReturn(workflowJob);

        this.sourceByItem.when(() -> SCMSource.SourceByItem.findSource(workflowJob)).thenReturn(source);

        this.scmRevisionAction.when(() -> SCMRevisionAction.getRevision(
            source,
            build
        )).thenReturn(new AbstractGitSCMSource.SCMRevisionImpl(new SCMHead("master"), "efjrigjaefgaefg8487"));

        this.tuleapConnector.when(() -> TuleapConnector.lookupScanCredentials(
            any(),
            any(),
            any()
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
    public void testItNotifiesTuleapIfTheRevisionIsATuleapBranchRevision() {
        final WorkflowRun build = mock(WorkflowRun.class);
        final TuleapSCMSource source = mock(TuleapSCMSource.class);
        final PrintStream logger = mock(PrintStream.class);
        final WorkflowJob workflowJob = mock(WorkflowJob.class);
        final TuleapAccessToken accessKey = mock(TuleapAccessToken.class);
        final TuleapGitRepository repository = new TuleapGitRepository();

        repository.setId(5);

        when(build.getParent()).thenReturn(workflowJob);
        this.scmRevisionAction.when(() -> SCMRevisionAction.getRevision(
            source,
            build
        )).thenReturn(new TuleapBranchSCMRevision(new TuleapBranchSCMHead("master"), "efjrigjaefgaefg8487"));
        when(source.getTuleapGitRepository()).thenReturn(repository);

        this.sourceByItem.when(() -> SCMSource.SourceByItem.findSource(workflowJob)).thenReturn(source);
        this.tuleapConnector.when(() -> TuleapConnector.lookupScanCredentials(
            any(),
            any(),
            any()
        )).thenReturn(accessKey);

        verify(this.gitApi, atMostOnce()).sendBuildStatus(
            "5",
            "efjrigjaefgaefg8487",
            TuleapBuildStatus.success,
            accessKey
        );

        this.notifier.sendBuildStatusToTuleap(build, logger, TuleapBuildStatus.success, source);
    }

    @Test
    public void testItNotifiesTuleapIfTheRevisionIsATuleapPullRequestRevision() {
        final WorkflowRun build = mock(WorkflowRun.class);
        final TuleapSCMSource source = mock(TuleapSCMSource.class);
        final PrintStream logger = mock(PrintStream.class);
        final WorkflowJob workflowJob = mock(WorkflowJob.class);
        final TuleapAccessToken accessKey = mock(TuleapAccessToken.class);
        final TuleapGitRepository repository = new TuleapGitRepository();

        repository.setId(5);

        when(build.getParent()).thenReturn(workflowJob);

        GitPullRequest gitPullRequest = this.getPullRequestStub();

        this.scmRevisionAction.when(() -> SCMRevisionAction.getRevision(
            source,
            build
        )).thenReturn(new TuleapPullRequestRevision(
            new TuleapPullRequestSCMHead(
                gitPullRequest,
                SCMHeadOrigin.DEFAULT,
                new TuleapBranchSCMHead("master"),
                1,
                1,
                "head_ref1"
            ),
            new TuleapBranchSCMRevision(new TuleapBranchSCMHead("master"), "efjrigjaefgaefg8487"),
            new TuleapBranchSCMRevision(new TuleapBranchSCMHead("pr1"), "dfsddsfsdf48")
        ));
        when(source.getTuleapGitRepository()).thenReturn(repository);

        this.sourceByItem.when(() -> SCMSource.SourceByItem.findSource(workflowJob)).thenReturn(source);
        this.tuleapConnector.when(() -> TuleapConnector.lookupScanCredentials(
            any(),
            any(),
            any()
        )).thenReturn(accessKey);

        verify(this.gitApi, atMostOnce()).sendBuildStatus(
            "5",
            "efjrigjaefgaefg8487",
            TuleapBuildStatus.success,
            accessKey
        );

        this.notifier.sendBuildStatusToTuleap(build, logger, TuleapBuildStatus.success, source);
    }

    private GitPullRequest getPullRequestStub() {
        return new GitPullRequest() {
            @Override
            public String getId() {
                return null;
            }

            @Override
            public String getTitle() {
                return null;
            }

            @Override
            public GitRepositoryReference getSourceRepository() {
                return null;
            }

            @Override
            public GitRepositoryReference getDestinationRepository() {
                return null;
            }

            @Override
            public String getSourceBranch() {
                return null;
            }

            @Override
            public String getDestinationBranch() {
                return null;
            }

            @Override
            public GitHead getHead() {
                return null;
            }
        };
    }
}
