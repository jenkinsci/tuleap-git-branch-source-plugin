package org.jenkinsci.plugins.tuleap_git_branch_source;

import jenkins.branch.OrganizationFolder;
import jenkins.plugins.git.traits.RefSpecsSCMSourceTrait;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.trait.SCMTrait;
import jenkins.scm.impl.trait.WildcardSCMSourceFilterTrait;
import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;

public class TuleapSCMNavigatorDefaultConfigurationTest {

    @ClassRule
    public static JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testItLoadsTheDefaultConfigurationTraitsAtTuleapProjectCreation() throws IOException {
        OrganizationFolder createdProject = jenkins.createProject(OrganizationFolder.class, "Tuleap Project with default config");

        createdProject.getSCMNavigators().add(new TuleapSCMNavigator("1"));
        SCMNavigator newInstance = createdProject.getSCMNavigators().get(0).getDescriptor().newInstance("1");

        assert newInstance != null;

        assertThat(newInstance.getTraits(),
            containsInAnyOrder(
                Matchers.<SCMTrait<?>>allOf(
                    instanceOf(WildcardSCMSourceFilterTrait.class),
                    hasProperty("includes", is("*")),
                    hasProperty("excludes", is(""))),
                Matchers.instanceOf(RefSpecsSCMSourceTrait.class)
            )
        );
    }
}
