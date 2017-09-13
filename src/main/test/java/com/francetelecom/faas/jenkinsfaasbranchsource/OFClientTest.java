package com.francetelecom.faas.jenkinsfaasbranchsource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.francetelecom.faas.jenkinsfaasbranchsource.config.OrangeForgeSettings;

import static org.hamcrest.Matchers.containsString;

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
	public void given__setup__when__get__project__then__return__projects_infos(){
		String response = client.getProject();
		Assert.assertThat(response, containsString("Forge as a Service"));
		Assert.assertThat(response, containsString("faas"));
	}

	@Test
	public void given__setup__when__get__project_git__then__return__git__repositories(){
		String response = client.getRepositories();
		Assert.assertThat(response, containsString("caod-manager"));
		Assert.assertThat(response, containsString("zaproxy-slave"));
	}
}
