package com.francetelecom.faas.jenkinsfaasbranchsource;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.plugins.git.GitSCM;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.trait.SCMBuilder;


public class OFSCMBuilder extends SCMBuilder<OFSCMBuilder, GitSCM> {

	private final String remote ;

	public OFSCMBuilder(SCMHead head, SCMRevision revision, String remote) {
		super(GitSCM.class, head, revision);
		this.remote = remote;

	}

	@NonNull
	@Override
	public GitSCM build() {
		return new GitSCM(remote);
	}
}
