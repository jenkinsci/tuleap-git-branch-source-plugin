package org.jenkinsci.plugins.tuleap_branch_source;

import java.util.Collections;


import org.hamcrest.Matchers;
import org.jenkinsci.plugins.tuleap_branch_source.trait.BranchDiscoveryTrait;
import org.jenkinsci.plugins.tuleap_branch_source.trait.UserForkRepositoryTrait;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import jenkins.model.Jenkins;
import jenkins.scm.api.trait.SCMTrait;
import jenkins.scm.impl.trait.RegexSCMSourceFilterTrait;
import jenkins.scm.impl.trait.WildcardSCMSourceFilterTrait;

public class TuleapSCMNavigatorTest {

    @ClassRule
    public static JenkinsRule r = new JenkinsRule();
    @Rule
    public TestName currentTestName = new TestName();
    private TuleapSCMNavigator instance;

    public TuleapSCMNavigator load() {
        return load(currentTestName.getMethodName());
    }

    private TuleapSCMNavigator load(String dataSet) {
        return (TuleapSCMNavigator) Jenkins.XSTREAM2.fromXML(
            getClass().getResource(getClass().getSimpleName()+ "/" + dataSet + ".xml"));
    }

    @Before
    public void setup(){
        this.instance = load();
    }

    @Test
    public void new_project_by_default(){
        assertThat(instance.id(), is("https://www.forge.orange-labs.fr/projects::3280"));
        assertThat(instance.getprojectId(), is("3280"));
        assertThat(instance.getCredentialsId(), is("fe09fd0e-7287-44a1-b0b5-746accd227c1"));
        assertThat(instance.getApiUri(), is("https://www.forge.orange-labs.fr/api"));
        assertThat(instance.getGitBaseUri(), is("https://www.forge.orange-labs.fr/plugins/git/"));
        assertThat(instance.getRepositories(), is(Collections.emptyMap()));
        assertThat(instance.getTraits(),
                   containsInAnyOrder(
                       Matchers.<SCMTrait<?>>allOf(
                           instanceOf(UserForkRepositoryTrait.class),
                           hasProperty("strategy", is(1))),
                       Matchers.<SCMTrait<?>>allOf(
                           instanceOf(BranchDiscoveryTrait.class)),
                       Matchers.<SCMTrait<?>>allOf(
                           instanceOf(WildcardSCMSourceFilterTrait.class),
                           hasProperty("includes", is("*")),
                           hasProperty("excludes", is("*")))
                   )
        );
        assertThat(instance.getTraits(), not(hasItem(Matchers.instanceOf(RegexSCMSourceFilterTrait.class))));
    }

    @Test
    public void with_excludes(){
        assertThat(instance.getTraits(), hasItem(
            Matchers.<SCMTrait<?>>allOf(
                instanceOf(WildcardSCMSourceFilterTrait.class),
                hasProperty("includes", is("*")),
                hasProperty("excludes", is("*tool* *doc* *chef*"))
            )
        ));
    }

    @Test
    public void with_includes(){
        assertThat(instance.getTraits(), hasItem(
            Matchers.<SCMTrait<?>>allOf(
                instanceOf(WildcardSCMSourceFilterTrait.class),
                hasProperty("includes", is("*manager*")),
                hasProperty("excludes", is("*"))
            )
        ));
    }

    @Test
    public void do_not_exclude_user_fork(){
        assertThat(instance.getTraits(), hasItem(
            Matchers.<SCMTrait<?>>allOf(
                instanceOf(UserForkRepositoryTrait.class),
                hasProperty("strategy", is(2)))
        ));
    }
}
