package org.jenkinsci.plugins.tuleap_branch_source;

import org.hamcrest.Matchers;
import org.jenkinsci.plugins.tuleap_branch_source.trait.BranchDiscoveryTrait;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import jenkins.scm.api.trait.SCMTrait;
import jenkins.scm.impl.trait.RegexSCMSourceFilterTrait;

public class TuleapSCMSourceTest extends TuleapBranchSourceTest<TuleapSCMSource> {

    @Test
    public void new_source_by_default() {
        assertThat(instance.getId(), is("https://www.forge.orange-labs.fr/projects::3280sample-simpleproject"));
        assertThat(instance.getprojectId(), is("3280"));
        assertThat(instance.getCredentialsId(), is("fe09fd0e-7287-44a1-b0b5-746accd227c1"));
        assertThat(instance.getApiBaseUri(), is("https://www.forge.orange-labs.fr/api"));
        assertThat(instance.getGitBaseUri(), is("https://www.forge.orange-labs.fr/plugins/git/"));
        assertThat(instance.getRemote(), is("https://www.forge.orange-labs.fr/plugins/git/faas/sample-simpleproject.git"));
        assertThat(instance.getRepositoryPath(), is("faas/sample-simpleproject.git"));
        assertThat(instance.getTraits(),
                                 containsInAnyOrder(
                                     Matchers.<SCMTrait<?>>allOf(
                                         instanceOf(BranchDiscoveryTrait.class))
                                 )
        );
        assertThat(instance.getTraits(), not(hasItem(Matchers.instanceOf(RegexSCMSourceFilterTrait.class))));
    }
}
