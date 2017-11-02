package com.francetelecom.faas.jenkinsfaasbranchsource.client.impl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapGitBranch;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapGitRepository;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapProject;
import com.francetelecom.faas.jenkinsfaasbranchsource.config.OrangeForgeSettings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertTrue;

public class DefaultClientTest {

    private DefaultClient client;
    private OrangeForgeSettings orangeForgeSettings;

    @Before
    public void setUp() throws Exception {
        orangeForgeSettings = new OrangeForgeSettings();
        client = new DefaultClient(orangeForgeSettings.credentials(), orangeForgeSettings.getApiBaseUrl(),
            orangeForgeSettings.getGitBaseUrl());
    }

    @After
    public void tearDown() throws Exception {
        client = null;
    }

    @Test
    public void given__setup__when__get__project__then__return__projects_infos() throws IOException {
        Optional<TuleapProject> project = client.projectById("3280");
        Assert.assertThat(project.get().getLabel(), is("Forge as a Service"));
        Assert.assertThat(project.get().getShortname(), is("faas"));
    }

    @Test
    public void given__setup__when__get__project_git__then__return__git__repositories() throws IOException {
        List<TuleapGitRepository> response = client.allProjectRepositories(orangeForgeSettings.getFaaSProjectId());
        assertThat(response,
            hasItem(Matchers.<TuleapGitRepository> hasProperty("name", equalToIgnoringCase("pkg/caod/caod-manager"))));
        assertThat(response, hasItem(
            Matchers.<TuleapGitRepository> hasProperty("name", equalToIgnoringCase("pkg/tools/zaproxy-slave"))));
    }

    @Test
    public void given__setup__when__get__project_gitBranch__then__return__git__branches() throws IOException {
        List<TuleapGitBranch> response = client.branchByGitRepo("faas/pkg/faas/faas-meta-packages.git");
        assertThat(response, hasItems(hasProperty("name", equalToIgnoringCase("refs/heads/develop"))));
        assertThat(response, hasItems(hasProperty("name", equalToIgnoringCase("refs/heads/master"))));
    }

    @Test
    public void given__setup__when__is__credententials__valid__then__return__true() throws IOException {
        assertTrue("credentials in setup (from src/test/orangeforge.properties) should be valid",
            client.isCredentialValid());
    }

    @Test
    public void given__setup__when__get__users__project__then__return__projects__userIsMemberOf() throws Exception {
        List<TuleapProject> projects = client.allUserProjects(true);
        assertThat(projects, hasItems(hasProperty("shortname", equalToIgnoringCase("faas"))));
    }
}
