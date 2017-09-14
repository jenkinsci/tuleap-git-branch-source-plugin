package com.francetelecom.faas.jenkinsfaasbranchsource;

import java.io.IOException;


import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.francetelecom.faas.jenkinsfaasbranchsource.config.OrangeForgeSettings;
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFGitRepository;
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFProject;
import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFProjectRepositories;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

/**
 * Created by qsqf2513 on 9/11/17.
 */
public class OFClientTest {


	private OFClient client;

	@Before
	public void setUp() throws Exception {
		client = new OFClient(new OrangeForgeSettings());
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void given__setup__when__get__project__then__return__projects_infos() throws IOException {
		OFProject project = client.project();
		Assert.assertThat(project.getLabel(), is("Forge as a Service"));
		Assert.assertThat(project.getShortname(), is("faas"));
	}

	@Test
	public void given__setup__when__get__project_git__then__return__git__repositories() throws IOException {
		OFProjectRepositories response = client.projectRepositories();
		assertThat(response.getRepositories(), hasItem(Matchers.<OFGitRepository>hasProperty(
				"name", equalToIgnoringCase("pkg/caod/caod-manager"))
		));
		assertThat(response.getRepositories(), hasItem(Matchers.<OFGitRepository>hasProperty(
				"name", equalToIgnoringCase ("pkg/tools/zaproxy-slave"))));
	}
}
