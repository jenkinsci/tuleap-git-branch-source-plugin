package com.francetelecom.faas.jenkinsfaasbranchsource.client.api;

public class TuleapGitCommit {

	private final String message, hash;
	private long dateMillis;

	public TuleapGitCommit(String message, String hash, long dateMillis) {
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
