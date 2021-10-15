package org.jenkinsci.plugins.tuleap_git_branch_source;

import jenkins.branch.OrganizationFolder;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import static org.junit.Assert.assertEquals;

public class TuleapSCMNavigatorDeserializationTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    @LocalData
    public void deserializationOfNewTuleapProjectIdAttributeWorks() {
        OrganizationFolder folder = (OrganizationFolder) jenkins.getInstance().getItemByFullName("step");
        TuleapSCMNavigator navigator = folder.getNavigators().get(TuleapSCMNavigator.class);

        assertEquals(navigator.getTuleapProjectId(), "101");
    }

    @Test
    @LocalData
    public void deserializationOfOldProjectIdAttributeWorks() {
        OrganizationFolder folder = (OrganizationFolder) jenkins.getInstance().getItemByFullName("step");
        TuleapSCMNavigator navigator = folder.getNavigators().get(TuleapSCMNavigator.class);

        assertEquals(navigator.getTuleapProjectId(), "101");
    }
}
