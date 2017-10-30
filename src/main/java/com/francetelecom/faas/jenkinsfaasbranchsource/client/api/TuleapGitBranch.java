package com.francetelecom.faas.jenkinsfaasbranchsource.client.api;

public class TuleapGitBranch {

    private final String name, sha1;

    public TuleapGitBranch(String name, String sha1) {
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
