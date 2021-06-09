package org.jenkinsci.plugins.tuleap_git_branch_source.webhook.processor;

import hudson.model.CauseAction;
import hudson.model.Queue;
import hudson.model.queue.QueueTaskFuture;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import jenkins.model.ParameterizedJobMixIn.ParameterizedJob;
import jenkins.scm.api.SCMNavigator;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapSCMNavigator;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.BranchNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.RepositoryNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.RepositoryScanFailedException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.TuleapProjectNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.model.WebHookRepresentation;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;


public class JobFinderTest {

    private OrganizationFolderRetrieverImpl organizationFolderRetriever;

    @Before
    public void setUp() {
        this.organizationFolderRetriever = mock(OrganizationFolderRetrieverImpl.class);
    }

    @Test(expected = TuleapProjectNotFoundException.class)
    public void testThrowsTuleapProjectNotFoundExceptionWhenThereIsNoTuleapOrganizationFolder() throws TuleapProjectNotFoundException, RepositoryNotFoundException, BranchNotFoundException, RepositoryScanFailedException {
        OrganizationFolder folderWithSeveralOrigin = mock(OrganizationFolder.class);
        when(folderWithSeveralOrigin.isSingleOrigin()).thenReturn(false);

        OrganizationFolder folderWithBadSCMNavigator = mock(OrganizationFolder.class);
        when(folderWithBadSCMNavigator.isSingleOrigin()).thenReturn(true);
        List<SCMNavigator> scmNavigatorList = new ArrayList<>();
        SCMNavigator classicNavigator = mock(SCMNavigator.class);
        scmNavigatorList.add(classicNavigator);
        when(folderWithBadSCMNavigator.getSCMNavigators()).thenReturn(scmNavigatorList);

        OrganizationFolder tuleapFolderWithAUnwantedName = mock(OrganizationFolder.class);
        when(tuleapFolderWithAUnwantedName.isSingleOrigin()).thenReturn(true);
        List<SCMNavigator> tuleapScmNavigatorList = new ArrayList<>();
        TuleapSCMNavigator tuleapNavigator = mock(TuleapSCMNavigator.class);
        tuleapScmNavigatorList.add(tuleapNavigator);
        when(tuleapFolderWithAUnwantedName.getSCMNavigators()).thenReturn(tuleapScmNavigatorList);
        when(tuleapNavigator.getTuleapProjectId()).thenReturn("204");

        WebHookRepresentation representation = mock(WebHookRepresentation.class);
        when(representation.getTuleapProjectId()).thenReturn("8");

        JobFinderImpl jobFinder = new JobFinderImpl(this.organizationFolderRetriever);
        jobFinder.triggerConcernedJob(representation);
    }

    @Test(expected = RepositoryNotFoundException.class)
    public void testThrowRepositoryNotFoundExceptionWhenTheRepositoryDoesNotExist() throws TuleapProjectNotFoundException, RepositoryNotFoundException, BranchNotFoundException, RepositoryScanFailedException {
        OrganizationFolder tuleapFolder = mock(OrganizationFolder.class);
        when(tuleapFolder.isSingleOrigin()).thenReturn(true);
        List<SCMNavigator> tuleapScmNavigatorList = new ArrayList<>();
        TuleapSCMNavigator tuleapNavigator = mock(TuleapSCMNavigator.class);
        tuleapScmNavigatorList.add(tuleapNavigator);
        when(tuleapFolder.getSCMNavigators()).thenReturn(tuleapScmNavigatorList);
        when(tuleapNavigator.getTuleapProjectId()).thenReturn("204");

        Stream<OrganizationFolder> stream = Stream.<OrganizationFolder>builder().add(tuleapFolder).build();
        when(this.organizationFolderRetriever.retrieveTuleapOrganizationFolders()).thenReturn(stream);

        WebHookRepresentation representation = mock(WebHookRepresentation.class);
        when(representation.getTuleapProjectId()).thenReturn("204");

        MultiBranchProject repositoryProject = mock(MultiBranchProject.class);
        verify(repositoryProject, never()).getItem(anyString());

        ParameterizedJob job = mock(ParameterizedJob.class);
        verify(job, never()).scheduleBuild2(anyInt(), any());

        verify(tuleapFolder, never()).getJob(representation.getRepositoryName());
        JobFinderImpl jobFinder = new JobFinderImpl(this.organizationFolderRetriever);
        jobFinder.triggerConcernedJob(representation);
    }

    @Test(expected = BranchNotFoundException.class)
    public void testThrowBranchNotFoundExceptionWhenTheBranchDoesNotExistEvenAfterARescanOfTheRepository() throws TuleapProjectNotFoundException, BranchNotFoundException, RepositoryNotFoundException, RepositoryScanFailedException {
        WebHookRepresentation representation = mock(WebHookRepresentation.class);
        when(representation.getTuleapProjectId()).thenReturn("204");
        when(representation.getRepositoryName()).thenReturn("C63");
        when(representation.getBranchName()).thenReturn("W204");

        OrganizationFolder tuleapFolder = mock(OrganizationFolder.class);
        when(tuleapFolder.isSingleOrigin()).thenReturn(true);
        List<SCMNavigator> tuleapScmNavigatorList = new ArrayList<>();
        TuleapSCMNavigator tuleapNavigator = mock(TuleapSCMNavigator.class);
        tuleapScmNavigatorList.add(tuleapNavigator);
        when(tuleapFolder.getSCMNavigators()).thenReturn(tuleapScmNavigatorList);
        when(tuleapNavigator.getTuleapProjectId()).thenReturn("204");


        Stream<OrganizationFolder> stream = Stream.<OrganizationFolder>builder().add(tuleapFolder).build();
        when(this.organizationFolderRetriever.retrieveTuleapOrganizationFolders()).thenReturn(stream);

        MultiBranchProject repositoryProject = mock(MultiBranchProject.class);
        when(tuleapFolder.getJob(representation.getRepositoryName())).thenReturn(repositoryProject);

        Queue.Item item = mock(Queue.Item.class);
        when(repositoryProject.scheduleBuild2(eq(0), any(CauseAction.class))).thenReturn(item);
        when(this.organizationFolderRetriever.retrieveBranchJobFromRepositoryName(repositoryProject, representation)).thenReturn(null, null);

        JobFinderImpl jobFinder = new JobFinderImpl(this.organizationFolderRetriever);
        jobFinder.triggerConcernedJob(representation);
    }

    @Test(expected = RepositoryScanFailedException.class)
    public void testThrowRepositoryScanFailedExceptionWhenTheRescanOfTheRepositoryFail() throws TuleapProjectNotFoundException, BranchNotFoundException, RepositoryNotFoundException, RepositoryScanFailedException {
        WebHookRepresentation representation = mock(WebHookRepresentation.class);
        when(representation.getTuleapProjectId()).thenReturn("204");
        when(representation.getRepositoryName()).thenReturn("C63");
        when(representation.getBranchName()).thenReturn("W204");

        OrganizationFolder tuleapFolder = mock(OrganizationFolder.class);
        when(tuleapFolder.isSingleOrigin()).thenReturn(true);
        List<SCMNavigator> tuleapScmNavigatorList = new ArrayList<>();
        TuleapSCMNavigator tuleapNavigator = mock(TuleapSCMNavigator.class);
        tuleapScmNavigatorList.add(tuleapNavigator);
        when(tuleapFolder.getSCMNavigators()).thenReturn(tuleapScmNavigatorList);
        when(tuleapNavigator.getTuleapProjectId()).thenReturn("204");


        Stream<OrganizationFolder> stream = Stream.<OrganizationFolder>builder().add(tuleapFolder).build();
        when(this.organizationFolderRetriever.retrieveTuleapOrganizationFolders()).thenReturn(stream);

        MultiBranchProject repositoryProject = mock(MultiBranchProject.class);
        when(tuleapFolder.getJob(representation.getRepositoryName())).thenReturn(repositoryProject);

        when(repositoryProject.scheduleBuild2(eq(0), any(CauseAction.class))).thenReturn(null);
        when(this.organizationFolderRetriever.retrieveBranchJobFromRepositoryName(repositoryProject, representation)).thenReturn(null);

        JobFinderImpl jobFinder = new JobFinderImpl(this.organizationFolderRetriever);
        jobFinder.triggerConcernedJob(representation);
    }

    @Test
    public void testTheBuildIsScheduledWhenTheBranchAlreadyExist() throws TuleapProjectNotFoundException, BranchNotFoundException, RepositoryNotFoundException, RepositoryScanFailedException {
        WebHookRepresentation representation = mock(WebHookRepresentation.class);
        when(representation.getTuleapProjectId()).thenReturn("204");
        when(representation.getRepositoryName()).thenReturn("C63");

        OrganizationFolder tuleapFolder = mock(OrganizationFolder.class);
        when(tuleapFolder.isSingleOrigin()).thenReturn(true);
        List<SCMNavigator> tuleapScmNavigatorList = new ArrayList<>();
        TuleapSCMNavigator tuleapNavigator = mock(TuleapSCMNavigator.class);
        tuleapScmNavigatorList.add(tuleapNavigator);
        when(tuleapFolder.getSCMNavigators()).thenReturn(tuleapScmNavigatorList);
        when(tuleapNavigator.getTuleapProjectId()).thenReturn("204");

        Stream<OrganizationFolder> stream = Stream.<OrganizationFolder>builder().add(tuleapFolder).build();
        when(this.organizationFolderRetriever.retrieveTuleapOrganizationFolders()).thenReturn(stream);

        MultiBranchProject repositoryProject = mock(MultiBranchProject.class);

        when(tuleapFolder.getJob(representation.getRepositoryName())).thenReturn(repositoryProject);

        ParameterizedJob branchJob = mock(ParameterizedJob.class);
        QueueTaskFuture task = mock(QueueTaskFuture.class);
        when(this.organizationFolderRetriever.retrieveBranchJobFromRepositoryName(repositoryProject, representation)).thenReturn(branchJob);
        when(branchJob.scheduleBuild2(eq(0), any(CauseAction.class))).thenReturn(task);

        JobFinderImpl jobFinder = new JobFinderImpl(this.organizationFolderRetriever);
        jobFinder.triggerConcernedJob(representation);
    }

    @Test
    public void testTheBuildIsScheduledWhenTheBranchIsDiscoverAfterRepositoryRescan() throws TuleapProjectNotFoundException, BranchNotFoundException, RepositoryNotFoundException, RepositoryScanFailedException {
        WebHookRepresentation representation = mock(WebHookRepresentation.class);
        when(representation.getTuleapProjectId()).thenReturn("204");
        when(representation.getRepositoryName()).thenReturn("C63");

        OrganizationFolder tuleapFolder = mock(OrganizationFolder.class);
        when(tuleapFolder.isSingleOrigin()).thenReturn(true);
        List<SCMNavigator> tuleapScmNavigatorList = new ArrayList<>();
        TuleapSCMNavigator tuleapNavigator = mock(TuleapSCMNavigator.class);
        tuleapScmNavigatorList.add(tuleapNavigator);
        when(tuleapFolder.getSCMNavigators()).thenReturn(tuleapScmNavigatorList);
        when(tuleapNavigator.getTuleapProjectId()).thenReturn("204");

        Stream<OrganizationFolder> stream = Stream.<OrganizationFolder>builder().add(tuleapFolder).build();
        when(this.organizationFolderRetriever.retrieveTuleapOrganizationFolders()).thenReturn(stream);

        MultiBranchProject repositoryProject = mock(MultiBranchProject.class);

        when(tuleapFolder.getJob(representation.getRepositoryName())).thenReturn(repositoryProject);

        ParameterizedJob branchJob = mock(ParameterizedJob.class);
        Queue.Item item = mock(Queue.Item.class);
        QueueTaskFuture task = mock(QueueTaskFuture.class);
        when(this.organizationFolderRetriever.retrieveBranchJobFromRepositoryName(repositoryProject, representation)).thenReturn(null, branchJob);
        when(repositoryProject.scheduleBuild2(eq(0), any(CauseAction.class))).thenReturn(item);
        when(branchJob.scheduleBuild2(eq(0), any(CauseAction.class))).thenReturn(task);

        JobFinderImpl jobFinder = new JobFinderImpl(this.organizationFolderRetriever);
        jobFinder.triggerConcernedJob(representation);
    }
}
