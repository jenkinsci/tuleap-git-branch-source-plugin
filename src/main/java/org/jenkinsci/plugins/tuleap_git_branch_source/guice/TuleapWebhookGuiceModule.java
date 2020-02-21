package org.jenkinsci.plugins.tuleap_git_branch_source.guice;

import com.google.inject.AbstractModule;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.check.TuleapWebHookChecker;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.check.TuleapWebHookCheckerImpl;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.processor.*;

public class TuleapWebhookGuiceModule extends AbstractModule {
    @Override
    public void configure() {
        bind(TuleapWebHookChecker.class).to(TuleapWebHookCheckerImpl.class);
        bind(TuleapWebHookProcessor.class).to(TuleapWebHookProcessorImpl.class);
        bind(JobFinder.class).to(JobFinderImpl.class);
        bind(OrganizationFolderRetriever.class).to(OrganizationFolderRetrieverImpl.class);
    }
}
