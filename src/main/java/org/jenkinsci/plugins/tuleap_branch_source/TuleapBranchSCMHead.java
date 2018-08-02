package org.jenkinsci.plugins.tuleap_branch_source;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMHead;

/**
 * Head corresponding to a branch of an Tuleap git repository.
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
        return Messages.BranchSCMHead_pronoun();
    }

}
