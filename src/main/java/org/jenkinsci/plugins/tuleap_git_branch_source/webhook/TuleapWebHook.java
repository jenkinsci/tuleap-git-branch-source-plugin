package org.jenkinsci.plugins.tuleap_git_branch_source.webhook;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import hudson.util.HttpResponses;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.annotation.CheckForNull;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class TuleapWebHook implements UnprotectedRootAction {

    private static final Logger LOGGER = Logger.getLogger(TuleapWebHook.class.getName());

    static final String WEBHOOK_URL = "tuleap-hook";

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return "Process request from Tuleap";
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return WEBHOOK_URL;
    }

    public HttpResponse doIndex(final StaplerRequest request, final StaplerResponse response) {
        LOGGER.log(Level.INFO, "Tuleap WebHook called with URL: {0} ", request.getRequestURIWithQueryString());
        return HttpResponses.ok();
    }

}
