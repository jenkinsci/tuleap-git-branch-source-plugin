package org.jenkinsci.plugins.tuleap_git_branch_source;

import jenkins.scm.impl.trait.RegexSCMSourceFilterTrait;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TuleapSCMSourceTest extends TuleapBranchSourceTest<TuleapSCMSource> {

    @Test
    public void new_source_by_default() {
        assertThat(instance.getId(), is("https://www.tuleap.example.test/projects::3280sample-simpleproject"));
        assertThat(instance.getprojectId(), is("3280"));
        assertThat(instance.getCredentialsId(), is("fe09fd0e-7287-44a1-b0b5-746accd227c1"));
        assertThat(instance.getApiBaseUri(), is("https://www.tuleap.example.test/api"));
        assertThat(instance.getGitBaseUri(), is("https://www.tuleap.example.test/plugins/git/"));
        assertThat(instance.getRemote(), is("https://www.tuleap.example.test/plugins/git/ttp/sample-simpleproject.git"));
        assertThat(instance.getRepositoryPath(), is("ttp/sample-simpleproject.git"));
        assertThat(instance.getTraits(), not(hasItem(Matchers.instanceOf(RegexSCMSourceFilterTrait.class))));
    }
}
