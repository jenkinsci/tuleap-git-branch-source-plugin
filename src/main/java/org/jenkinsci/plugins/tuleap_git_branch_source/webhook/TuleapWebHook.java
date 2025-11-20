package org.jenkinsci.plugins.tuleap_git_branch_source.webhook;

import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import org.jenkinsci.plugins.tuleap_git_branch_source.guice.TuleapWebhookGuiceModule;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.processor.TuleapWebHookProcessor;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class TuleapWebHook implements UnprotectedRootAction {

    private static final Logger LOGGER = Logger.getLogger(TuleapWebHook.class.getName());

    static final String WEBHOOK_URL = "tuleap-hook";

    private TuleapWebHookProcessor tuleapWebHookActionProcessor;

    public TuleapWebHook() {
        Injector injector = Guice.createInjector(new TuleapWebhookGuiceModule());
        this.tuleapWebHookActionProcessor = injector.getInstance(TuleapWebHookProcessor.class);
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return "Process request from Tuleap git repository";
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return WEBHOOK_URL;
    }

    public HttpResponse doIndex(final StaplerRequest2 request, final StaplerResponse2 response) throws IOException {
        LOGGER.log(Level.FINEST, "Tuleap WebHook called with URL: {0} ", request.getRequestURIWithQueryString());
        return this.tuleapWebHookActionProcessor.process(request);
    }
}
