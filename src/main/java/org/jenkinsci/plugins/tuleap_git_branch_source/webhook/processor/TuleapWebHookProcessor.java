package org.jenkinsci.plugins.tuleap_git_branch_source.webhook.processor;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest2;

import java.io.IOException;

public interface TuleapWebHookProcessor {
    HttpResponse process(StaplerRequest2 request) throws IOException;
}
