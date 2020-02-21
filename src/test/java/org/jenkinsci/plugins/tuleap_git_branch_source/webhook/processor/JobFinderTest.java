package org.jenkinsci.plugins.tuleap_git_branch_source.webhook.processor;

import hudson.model.CauseAction;
import hudson.model.queue.QueueTaskFuture;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import jenkins.model.ParameterizedJobMixIn.ParameterizedJob;
import jenkins.scm.api.SCMNavigator;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapSCMNavigator;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.BranchNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.RepositoryNotFoundException;
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
    public void testThrowsTuleapProjectNotFoundExceptionWhenThereIsNoTuleapOrganizationFolder() throws TuleapProjectNotFoundException, RepositoryNotFoundException, BranchNotFoundException {
        OrganizationFolder folderWithSeveralOrigin = mock(OrganizationFolder.class);
        when(folderWithSeveralOrigin.getName()).thenReturn("Bayerische-Motoren-Werke");
        when(folderWithSeveralOrigin.isSingleOrigin()).thenReturn(false);

        OrganizationFolder folderWithBadSCMNavigator = mock(OrganizationFolder.class);
        when(folderWithBadSCMNavigator.getName()).thenReturn("Fabbrica Italiana Automobili Torino");
        when(folderWithBadSCMNavigator.isSingleOrigin()).thenReturn(true);
        List<SCMNavigator> scmNavigatorList = new ArrayList<>();
        SCMNavigator classicNavigator = mock(SCMNavigator.class);
        scmNavigatorList.add(classicNavigator);
        when(folderWithBadSCMNavigator.getSCMNavigators()).thenReturn(scmNavigatorList);

        OrganizationFolder tuleapFolderWithAUnwantedName = mock(OrganizationFolder.class);
        when(tuleapFolderWithAUnwantedName.getName()).thenReturn("Aufrecht-Melcher-Großaspach");
        when(tuleapFolderWithAUnwantedName.isSingleOrigin()).thenReturn(true);
        List<SCMNavigator> tuleapScmNavigatorList = new ArrayList<>();
        TuleapSCMNavigator tuleapNavigator = mock(TuleapSCMNavigator.class);
        tuleapScmNavigatorList.add(tuleapNavigator);
        when(tuleapFolderWithAUnwantedName.getSCMNavigators()).thenReturn(tuleapScmNavigatorList);

        WebHookRepresentation representation = mock(WebHookRepresentation.class);
        when(representation.getTuleapProjectName()).thenReturn("Tata");

        JobFinderImpl jobFinder = new JobFinderImpl(this.organizationFolderRetriever);
        jobFinder.triggerConcernedJob(representation);
    }

    @Test(expected = RepositoryNotFoundException.class)
    public void testThrowRepositoryNotFoundExceptionWhenTheRepositoryDoesNotExist() throws TuleapProjectNotFoundException, RepositoryNotFoundException, BranchNotFoundException {
        OrganizationFolder tuleapFolder = mock(OrganizationFolder.class);
        when(tuleapFolder.getName()).thenReturn("Aufrecht-Melcher-Großaspach");
        when(tuleapFolder.isSingleOrigin()).thenReturn(true);
        List<SCMNavigator> tuleapScmNavigatorList = new ArrayList<>();
        TuleapSCMNavigator tuleapNavigator = mock(TuleapSCMNavigator.class);
        tuleapScmNavigatorList.add(tuleapNavigator);
        when(tuleapFolder.getSCMNavigators()).thenReturn(tuleapScmNavigatorList);

        Stream<OrganizationFolder> stream = Stream.<OrganizationFolder>builder().add(tuleapFolder).build();
        when(this.organizationFolderRetriever.retrieveTuleapOrganizationFolders()).thenReturn(stream);

        WebHookRepresentation representation = mock(WebHookRepresentation.class);
        when(representation.getTuleapProjectName()).thenReturn("Aufrecht-Melcher-Großaspach");

        MultiBranchProject multiBranchProject = mock(MultiBranchProject.class);
        verify(multiBranchProject, never()).getItem(anyString());

        ParameterizedJob job = mock(ParameterizedJob.class);
        verify(job, never()).scheduleBuild2(anyInt(), any());

        verify(tuleapFolder, never()).getJob(representation.getRepositoryName());
        JobFinderImpl jobFinder = new JobFinderImpl(this.organizationFolderRetriever);
        jobFinder.triggerConcernedJob(representation);
    }

    @Test(expected = BranchNotFoundException.class)
    public void testThrowBranchNotFoundExceptionWhenTheBranchDoesNotExist() throws TuleapProjectNotFoundException, BranchNotFoundException, RepositoryNotFoundException {
        WebHookRepresentation representation = mock(WebHookRepresentation.class);
        when(representation.getTuleapProjectName()).thenReturn("Aufrecht-Melcher-Großaspach");
        when(representation.getRepositoryName()).thenReturn("C63");
        when(representation.getBranchName()).thenReturn("W204");

        OrganizationFolder tuleapFolder = mock(OrganizationFolder.class);
        when(tuleapFolder.getName()).thenReturn("Aufrecht-Melcher-Großaspach");
        when(tuleapFolder.isSingleOrigin()).thenReturn(true);
        List<SCMNavigator> tuleapScmNavigatorList = new ArrayList<>();
        TuleapSCMNavigator tuleapNavigator = mock(TuleapSCMNavigator.class);
        tuleapScmNavigatorList.add(tuleapNavigator);
        when(tuleapFolder.getSCMNavigators()).thenReturn(tuleapScmNavigatorList);


        Stream<OrganizationFolder> stream = Stream.<OrganizationFolder>builder().add(tuleapFolder).build();
        when(this.organizationFolderRetriever.retrieveTuleapOrganizationFolders()).thenReturn(stream);

        ParameterizedJob job = mock(ParameterizedJob.class);
        verify(job, never()).scheduleBuild2(anyInt(), any());

        MultiBranchProject multiBranchProject = mock(MultiBranchProject.class);
        when(tuleapFolder.getJob(representation.getRepositoryName())).thenReturn(multiBranchProject);

        JobFinderImpl jobFinder = new JobFinderImpl(this.organizationFolderRetriever);
        jobFinder.triggerConcernedJob(representation);
    }

    @Test
    public void testTheBuildIsScheduled() throws TuleapProjectNotFoundException, BranchNotFoundException, RepositoryNotFoundException {
        WebHookRepresentation representation = mock(WebHookRepresentation.class);
        when(representation.getTuleapProjectName()).thenReturn("Aufrecht-Melcher-Großaspach");
        when(representation.getRepositoryName()).thenReturn("C63");

        OrganizationFolder tuleapFolder = mock(OrganizationFolder.class);
        when(tuleapFolder.getName()).thenReturn("Aufrecht-Melcher-Großaspach");
        when(tuleapFolder.isSingleOrigin()).thenReturn(true);
        List<SCMNavigator> tuleapScmNavigatorList = new ArrayList<>();
        TuleapSCMNavigator tuleapNavigator = mock(TuleapSCMNavigator.class);
        tuleapScmNavigatorList.add(tuleapNavigator);
        when(tuleapFolder.getSCMNavigators()).thenReturn(tuleapScmNavigatorList);

        Stream<OrganizationFolder> stream = Stream.<OrganizationFolder>builder().add(tuleapFolder).build();
        when(this.organizationFolderRetriever.retrieveTuleapOrganizationFolders()).thenReturn(stream);

        MultiBranchProject multiBranchProject = mock(MultiBranchProject.class);

        when(tuleapFolder.getJob(representation.getRepositoryName())).thenReturn(multiBranchProject);

        ParameterizedJob job = mock(ParameterizedJob.class);
        QueueTaskFuture task = mock(QueueTaskFuture.class);
        when(this.organizationFolderRetriever.retrieveBranchJobFromRepositoryName(multiBranchProject, representation)).thenReturn(job);
        when(job.scheduleBuild2(eq(0), any(CauseAction.class))).thenReturn(task);

        JobFinderImpl jobFinder = new JobFinderImpl(this.organizationFolderRetriever);
        jobFinder.triggerConcernedJob(representation);
    }
}
