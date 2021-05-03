package org.jenkinsci.plugins.tuleap_git_branch_source.webhook;

import hudson.model.Cause;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.model.WebHookRepresentation;

public class TuleapWebhookRetriggerRepositoryScanCause extends Cause {

    private WebHookRepresentation representation;

    public TuleapWebhookRetriggerRepositoryScanCause(WebHookRepresentation representation) {
        this.representation = representation;
    }

    @Override
    public String getShortDescription() {
        return String.format("Scan the repository to retrieve the job corresponding to the branch '%s'", this.representation.getBranchName());
    }
}
