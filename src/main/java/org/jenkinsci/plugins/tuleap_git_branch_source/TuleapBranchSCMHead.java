package org.jenkinsci.plugins.tuleap_git_branch_source;

import jenkins.scm.api.SCMHead;

import javax.annotation.Nonnull;

/**
 * Head corresponding to a branch of an Tuleap git repository.
 */
public class TuleapBranchSCMHead extends SCMHead {

    /**
     * {@inheritDoc}
     */
    public TuleapBranchSCMHead(@Nonnull String name) {
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
