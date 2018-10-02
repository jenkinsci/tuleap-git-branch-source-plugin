package org.jenkinsci.plugins.tuleap_git_branch_source.trait;

import java.io.IOException;


import org.kohsuke.stapler.DataBoundConstructor;

import org.jenkinsci.plugins.tuleap_git_branch_source.Messages;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapBranchSCMHead;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapSCMSource;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapSCMSourceContext;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapSCMSourceRequest;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMHeadAuthority;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.impl.trait.Discovery;

public class BranchDiscoveryTrait extends SCMSourceTrait {

    @DataBoundConstructor
    public BranchDiscoveryTrait() {

    }

    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        TuleapSCMSourceContext ctx = (TuleapSCMSourceContext) context;
        ctx.wantBranches(true);
        // ctx.withAuthority(new OFBranchSCMHeadAuthority());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean includeCategory(@NonNull SCMHeadCategory category) {
        return category.isUncategorized();
    }

    /**
     * The descriptor.
     */
    @Extension
    @Discovery
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.BranchDiscoveryTrait_displayName();
        }

        @Override
        public Class<? extends SCMSourceContext> getContextClass() {
            return TuleapSCMSourceContext.class;
        }

        @Override
        public Class<? extends SCMSource> getSourceClass() {
            return TuleapSCMSource.class;
        }
    }

    public static class OFBranchSCMHeadAuthority
        extends SCMHeadAuthority<TuleapSCMSourceRequest, TuleapBranchSCMHead, SCMRevision> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean checkTrusted(@NonNull TuleapSCMSourceRequest tuleapSCMSourceRequest,
            @NonNull TuleapBranchSCMHead tuleapBranchSCMHead) throws IOException, InterruptedException {
            return true;
        }
    }

    /*
     * private class ExcludeBranchesSCMHeadFilter extends SCMHeadFilter {
     *
     * @Override public boolean isExcluded(@NonNull SCMSourceRequest request, @NonNull SCMHead head) throws IOException,
     * InterruptedException { return false; } }
     */
}
