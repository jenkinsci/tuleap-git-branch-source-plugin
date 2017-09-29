package com.francetelecom.faas.jenkinsfaasbranchsource.ofapi;

public class OFGitCommit {

	private final String message, hash;
	private long dateMillis;

	public OFGitCommit(String message, String hash, long dateMillis) {
		this.message = message;
		this.hash = hash;
		this.dateMillis = dateMillis;
	}

	public String getMessage() {
		return message;
	}

	public String getHash() {
		return hash;
	}

	public long getDateMillis() {
		return dateMillis;
	}
}
