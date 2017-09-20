package com.francetelecom.faas.jenkinsfaasbranchsource.ofapi;

public class OFGitBranch {

	private final String name, sha1;

	public OFGitBranch(String name, String sha1) {
		this.name = name;
		this.sha1 = sha1;
	}

	public String getName() {
		return name;
	}

	public String getSha1() {
		return sha1;
	}
}
