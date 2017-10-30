package com.francetelecom.faas.jenkinsfaasbranchsource;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.trait.SCMSourceBuilder;

public class TuleapSCMSourceBuilder extends SCMSourceBuilder<TuleapSCMSourceBuilder, TuleapSCMSource> {
    private final String id;
    private final String credentialsId;
    private final String projectId;

    public TuleapSCMSourceBuilder(String id, String credentialsId, String projectId, String repositoryPath) {
        super(TuleapSCMSource.class, repositoryPath);
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

    // projectName is the representation of a repo git in the context of a SCM
    @NonNull
    @Override
    public TuleapSCMSource build() {
        TuleapSCMSource result = new TuleapSCMSource(projectId(), projectName());
        result.setId(id());
        result.setTraits(traits());
        result.setCredentialsId(credentialsId());
        return result;
    }
}
