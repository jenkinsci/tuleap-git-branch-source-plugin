package org.jenkinsci.plugins.tuleap_git_branch_source.webhook.processor;

import com.google.gson.Gson;
import hudson.util.HttpResponses;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.check.TuleapWebHookChecker;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.BranchNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.RepositoryNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.TuleapProjectNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.helper.TuleapWebHookHelper;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.model.WebHookRepresentation;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TuleapWebHookProcessorImpl implements TuleapWebHookProcessor {

    private static final Logger LOGGER = Logger.getLogger(TuleapWebHookProcessor.class.getName());

    private final Gson gson;

    private TuleapWebHookChecker webHookChecker;

    private JobFinder jobFinder;

    private TuleapWebHookHelper helper;

    @Inject
    public TuleapWebHookProcessorImpl(Gson gson, TuleapWebHookChecker webHookChecker, JobFinder jobFinder, TuleapWebHookHelper helper) {
        this.gson = gson;
        this.webHookChecker = webHookChecker;
        this.jobFinder = jobFinder;
        this.helper = helper;
    }

    public HttpResponse process(StaplerRequest request) throws IOException {
        if (!this.webHookChecker.checkRequestHeaderContentType(request.getContentType())) {
            return HttpResponses.error(400, "Content type not supported");
        }

        String payload = helper.getStringPayload(request);

        if (payload.isEmpty()) {
            return HttpResponses.error(400, "Jenkins job cannot be triggered. The request is empty");
        }

        String decodedPayload;
        try {
            decodedPayload = helper.getUTF8DecodedPayload(payload);
        } catch (UnsupportedEncodingException e) {
            return HttpResponses.error(500, "Error while decoding the payload");
        }

        LOGGER.log(Level.FINEST, "Checking the payload content...");

        WebHookRepresentation representation = this.gson.fromJson(decodedPayload, WebHookRepresentation.class);
        if (representation == null || !this.webHookChecker.checkPayloadContent(representation)){
            LOGGER.log(Level.WARNING, "Bad payload format");
            return HttpResponses.error(400, "Bad payload format");
        }
        try {
            this.jobFinder.triggerConcernedJob(representation);
        } catch (RepositoryNotFoundException | BranchNotFoundException | TuleapProjectNotFoundException e) {
           e.printStackTrace();
        }

        return HttpResponses.ok();
    }
}
