package org.jenkinsci.plugins.tuleap_git_branch_source.webhook.check;

import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.model.WebHookRepresentation;

public class TuleapWebHookCheckerImpl implements TuleapWebHookChecker {

    public boolean checkRequestHeaderContentType(String contentType) {
        return contentType != null && contentType.equals("application/x-www-form-urlencoded");
    }

    public boolean checkPayloadContent(WebHookRepresentation representation) {
        return representation.getBranchName() != null && representation.getRepositoryName() != null && representation.getTuleapProjectName() != null;
    }
}
