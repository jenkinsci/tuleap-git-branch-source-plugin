package com.francetelecom.faas.jenkinsfaasbranchsource.trait;

import java.io.IOException;

import javax.annotation.Nonnull;


import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.francetelecom.faas.jenkinsfaasbranchsource.Messages;
import com.francetelecom.faas.jenkinsfaasbranchsource.OFSCMNavigatorContext;
import com.francetelecom.faas.jenkinsfaasbranchsource.OFSCMSource;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.ListBoxModel;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMNavigatorContext;
import jenkins.scm.api.trait.SCMNavigatorRequest;
import jenkins.scm.api.trait.SCMNavigatorTrait;
import jenkins.scm.api.trait.SCMNavigatorTraitDescriptor;
import jenkins.scm.api.trait.SCMSourceFilter;
import jenkins.scm.api.trait.SCMSourcePrefilter;
import jenkins.scm.impl.trait.Discovery;


public class OFUserForkRepositoryTrait extends SCMNavigatorTrait {

	private int strategyId;

	@DataBoundConstructor
	public OFUserForkRepositoryTrait(int strategyId) {
		if (strategyId == 0) {
			strategyId = 1;
		}
		this.strategyId = strategyId;
	}

	public int getStrategyId() {
		return strategyId;
	}

	@DataBoundSetter
	public void setStrategyId(int strategyId) {
		this.strategyId = strategyId;
	}

	@Override
	protected void decorateContext(SCMNavigatorContext<?, ?> ctx) {
		OFSCMNavigatorContext context = (OFSCMNavigatorContext) ctx;
		context.withFilter(new ExcludeNotOwnedRepositoryFilter());
		context.wantUserFork(strategyId == 1);
		if (strategyId == 1) {
			context.withPrefilter(new ExcludeUserForkRepositorySCMFilter());
		} else {
			//we don't care if it is a userFork or not, we're taking them all, no need to filter
		}
	}

	private class ExcludeUserForkRepositorySCMFilter extends SCMSourcePrefilter {

		@Override
		public boolean isExcluded(@NonNull SCMNavigator source, @NonNull String projectName) {
			return projectName.contains("/u/");
		}
	}

	private class ExcludeNotOwnedRepositoryFilter extends SCMSourceFilter {

		@Override
		public boolean isExcluded(@NonNull SCMNavigatorRequest request, @NonNull String projectName) throws
				IOException, InterruptedException {
			//TODO either ask orangeforge if authenticated user can read repo = projectName
			//or better when OFClient.projectRepositories only return projeect that we can access
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean includeCategory(@NonNull SCMHeadCategory category) {
		return category.isUncategorized();
	}


	@Extension
	@Discovery
	public static class DescriptorImpl extends SCMNavigatorTraitDescriptor {
		@Nonnull
		@Override
		public String getDisplayName() {
			return "Exclure les fork utilisateurs";
		}

		@Override
		public Class<? extends SCMNavigatorContext> getContextClass() {
			return OFSCMNavigatorContext.class;
		}

		@Override
		public Class<? extends SCMSource> getSourceClass() {
			return OFSCMSource.class;
		}

		@NonNull
		@Restricted(NoExternalUse.class)
		@SuppressWarnings("unused") // stapler
		public ListBoxModel doFillStrategyIdItems() {
			ListBoxModel result = new ListBoxModel();
			result.add(Messages.OFUserForkRepositoryTrait_excludeUserForkRepositories(), "1");
			result.add(Messages.OFUserForkRepositoryTrait_allRepositories(), "2");
			return result;
		}
	}
}
