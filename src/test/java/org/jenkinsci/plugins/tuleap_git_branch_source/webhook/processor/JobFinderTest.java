package org.jenkinsci.plugins.tuleap_git_branch_source.webhook.processor;

import jenkins.branch.OrganizationFolder;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.BranchNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.RepositoryNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.TuleapProjectNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.model.WebHookRepresentation;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.LoggerRule;

import java.io.*;

import static org.hamcrest.Matchers.containsString;

import java.util.logging.Level;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


public class JobFinderTest {

    @ClassRule
    public static JenkinsRule jenkins = new JenkinsRule();

    @Rule
    public LoggerRule loggerRule = new LoggerRule();

    @Test(expected = TuleapProjectNotFoundException.class)
    public void testItShouldThrowTuleapProjectNotFoundExceptionWhenTheProjectDoesNotExist() throws IOException, TuleapProjectNotFoundException, BranchNotFoundException, RepositoryNotFoundException {
        jenkins.createProject(OrganizationFolder.class, "Bayerische Motoren Werke");
        jenkins.createProject(OrganizationFolder.class, "Koenigseggddsfsdgevb");
        JobFinderImpl finder = new JobFinderImpl();
        WebHookRepresentation representation = mock(WebHookRepresentation.class);
        when(representation.getTuleapProjectName()).thenReturn("Aufrecht-Melcher-Großaspach");
        finder.triggerConcernedJob(representation);
        assertThat(this.loggerRule, LoggerRule.recorded(Level.INFO, containsString("This folder has no project type or has several project types")));
    }
}
