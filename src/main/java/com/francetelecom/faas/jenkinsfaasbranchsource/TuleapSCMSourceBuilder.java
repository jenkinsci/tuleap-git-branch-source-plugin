package com.francetelecom.faas.jenkinsfaasbranchsource;

import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapGitRepository;
import com.francetelecom.faas.jenkinsfaasbranchsource.client.api.TuleapProject;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.trait.SCMSourceBuilder;

public class TuleapSCMSourceBuilder extends SCMSourceBuilder<TuleapSCMSourceBuilder, TuleapSCMSource> {
    private final String id;
    private final String credentialsId;
    private final TuleapProject project;
    private final TuleapGitRepository repository;

    public TuleapSCMSourceBuilder(String id, String credentialsId, TuleapProject project, TuleapGitRepository repository) {
        super(TuleapSCMSource.class, repository.getPath());
        this.id = id;
        this.credentialsId = credentialsId;
        this.project = project;
        this.repository = repository;
    }

    public String id() {
        return id;
    }

    public String credentialsId() {
        return credentialsId;
    }

    // projectName is the representation of a repo git in the context of a SCM
    @NonNull
    @Override
    public TuleapSCMSource build() {
        TuleapSCMSource result = new TuleapSCMSource(project, repository);
        result.setId(id());
        result.setTraits(traits());
        result.setCredentialsId(credentialsId());
        return result;
    }
}
