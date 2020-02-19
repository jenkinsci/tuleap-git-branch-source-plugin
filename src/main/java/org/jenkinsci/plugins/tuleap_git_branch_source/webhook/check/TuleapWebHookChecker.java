package org.jenkinsci.plugins.tuleap_git_branch_source.webhook.check;

import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.model.WebHookRepresentation;

public interface TuleapWebHookChecker {
    boolean checkRequestHeaderContentType(String contentType);
    boolean checkPayloadContent(WebHookRepresentation content);
}
