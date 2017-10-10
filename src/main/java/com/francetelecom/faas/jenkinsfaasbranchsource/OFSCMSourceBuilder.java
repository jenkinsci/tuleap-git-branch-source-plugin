package com.francetelecom.faas.jenkinsfaasbranchsource;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.trait.SCMSourceBuilder;


public class OFSCMSourceBuilder extends SCMSourceBuilder<OFSCMSourceBuilder, OFSCMSource> {
	private final String id;
	private final String credentialsId;
	private final String projectId;

	public OFSCMSourceBuilder(String id, String credentialsId, String projectId, String repositoryName) {
		super(OFSCMSource.class, repositoryName);
		this.id = id;
		this.credentialsId = credentialsId;
		this.projectId = projectId;
	}

	public String id() {
		return id;
	}

	public String credentialsId() {
		return credentialsId;
	}

	public String projectId() {
		return projectId;
	}

	//projectName is the representation of a repo git in the context of a SCM
	@NonNull
	@Override
	public OFSCMSource build() {
		OFSCMSource result = new OFSCMSource(id(), projectId(), projectName());
		//TODO use below when moving to 2.60 with org.jenkins-ci.plugins.git:3.5.0
		//result.setId(id());
		result.setTraits(traits());
		result.setCredentialsId(credentialsId());
		return result;
	}
}
