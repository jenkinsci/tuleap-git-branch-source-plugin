package org.jenkinsci.plugins.tuleap_git_branch_source.trait;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.mixin.ChangeRequestSCMHead2;
import jenkins.scm.api.trait.*;
import jenkins.scm.impl.trait.Discovery;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.tuleap_git_branch_source.*;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

public class TuleapForkPullRequestDiscoveryTrait extends SCMSourceTrait {

    @DataBoundConstructor
    public TuleapForkPullRequestDiscoveryTrait() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        TuleapSCMSourceContext tuleapSourceContext = (TuleapSCMSourceContext) context;
        tuleapSourceContext.wantForkPullRequests(true);
        tuleapSourceContext.withAuthority(new TuleapForkPullRequestDiscoveryTrait.TrustNobody());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean includeCategory(@NonNull SCMHeadCategory category) {
        return category.isUncategorized();
    }

    @Symbol("tuleapForkPullRequestDiscovery")
    @Extension
    @Discovery
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.TuleapForkPullRequestDiscoveryTrait_displayName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<? extends SCMSourceContext> getContextClass() {
            return TuleapSCMSourceContext.class;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<? extends SCMSource> getSourceClass() {
            return TuleapSCMSource.class;
        }
    }

    public static class TrustNobody extends SCMHeadAuthority<SCMSourceRequest, ChangeRequestSCMHead2, SCMRevision> {

        @Override
        protected boolean checkTrusted(@NotNull SCMSourceRequest request, @NotNull ChangeRequestSCMHead2 head) throws IOException, InterruptedException {
            return false;
        }

        @Extension
        public static class DescriptorImpl extends SCMHeadAuthorityDescriptor {

            @Override
            public boolean isApplicableToOrigin(
                @NonNull Class<? extends SCMHeadOrigin> originClass) {
                return SCMHeadOrigin.Fork.class.isAssignableFrom(originClass);
            }
        }
    }
}
