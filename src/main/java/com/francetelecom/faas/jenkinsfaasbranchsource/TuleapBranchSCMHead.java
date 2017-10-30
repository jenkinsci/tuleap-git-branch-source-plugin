package com.francetelecom.faas.jenkinsfaasbranchsource;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMHead;

/**
 * Head corresponding to a branch of an OrangeForge git repository.
 */
public class TuleapBranchSCMHead extends SCMHead {

    /**
     * {@inheritDoc}
     */
    public TuleapBranchSCMHead(@NonNull String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPronoun() {
        return Messages.OFBranchSCMHead_pronoun();
    }

}
