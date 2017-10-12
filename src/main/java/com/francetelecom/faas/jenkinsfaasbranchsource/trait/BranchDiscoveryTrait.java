package com.francetelecom.faas.jenkinsfaasbranchsource.trait;

import java.io.IOException;


import org.kohsuke.stapler.DataBoundConstructor;

import com.francetelecom.faas.jenkinsfaasbranchsource.Messages;
import com.francetelecom.faas.jenkinsfaasbranchsource.OFBranchSCMHead;
import com.francetelecom.faas.jenkinsfaasbranchsource.OFSCMSource;
import com.francetelecom.faas.jenkinsfaasbranchsource.OFSCMSourceContext;
import com.francetelecom.faas.jenkinsfaasbranchsource.OFSCMSourceRequest;

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
		OFSCMSourceContext ctx = (OFSCMSourceContext) context;
		ctx.wantBranches(true);
		//ctx.withAuthority(new OFBranchSCMHeadAuthority());
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
			return Messages.OFBranchDiscoveryTrait_displayName();
		}

		@Override
		public Class<? extends SCMSourceContext> getContextClass() {
			return OFSCMSourceContext.class;
		}

		@Override
		public Class<? extends SCMSource> getSourceClass() {
			return OFSCMSource.class;
		}
	}

	public static class OFBranchSCMHeadAuthority extends SCMHeadAuthority<OFSCMSourceRequest, OFBranchSCMHead,
			SCMRevision> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean checkTrusted(@NonNull OFSCMSourceRequest ofscmSourceRequest, @NonNull OFBranchSCMHead
				ofBranchSCMHead) throws IOException, InterruptedException {
			return true;
		}
	}

	/*private class ExcludeBranchesSCMHeadFilter extends SCMHeadFilter {
		@Override
		public boolean isExcluded(@NonNull SCMSourceRequest request, @NonNull SCMHead head) throws IOException,
				InterruptedException {
			return false;
		}
	}*/
}
