package org.jenkinsci.plugins.tuleap_git_branch_source.webhook;

import hudson.model.Cause;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.model.WebHookRepresentation;

public class TuleapWebHookCause extends Cause {

    private WebHookRepresentation representation;

    public TuleapWebHookCause(WebHookRepresentation representation) {
        this.representation = representation;
    }

    @Override
    public String getShortDescription() {
        return String.format("The job was triggered by '%s' git repository from Tuleap", this.representation.getRepositoryName());
    }
}
