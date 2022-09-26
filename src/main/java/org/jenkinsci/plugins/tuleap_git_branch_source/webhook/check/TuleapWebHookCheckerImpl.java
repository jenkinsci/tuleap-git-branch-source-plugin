package org.jenkinsci.plugins.tuleap_git_branch_source.webhook.check;

import com.google.inject.Inject;
import io.jenkins.plugins.tuleap_api.client.authentication.WebhookTokenApi;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.model.WebHookRepresentation;

public class TuleapWebHookCheckerImpl implements TuleapWebHookChecker {
    private final WebhookTokenApi webhookTokenApi;

    @Inject
    public TuleapWebHookCheckerImpl(final WebhookTokenApi webhookTokenApi) {
        this.webhookTokenApi = webhookTokenApi;
    }
    public boolean checkRequestHeaderContentType(String contentType) {
        return contentType != null && contentType.equals("application/x-www-form-urlencoded");
    }

    public boolean checkPayloadContent(WebHookRepresentation representation) {
        return representation.getToken() != null &&
            representation.getBranchName() != null &&
            representation.getRepositoryName() != null &&
            representation.getTuleapProjectId() != null;
    }

    @Override
    public boolean checkRequestToken(WebHookRepresentation representation) {
        return this.webhookTokenApi.checkWebhookTokenIsValid(representation.getToken());
    }
}
