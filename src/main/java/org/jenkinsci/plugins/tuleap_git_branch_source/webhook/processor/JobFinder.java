package org.jenkinsci.plugins.tuleap_git_branch_source.webhook.processor;

import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.BranchNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.TuleapProjectNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.RepositoryNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.model.WebHookRepresentation;

public interface JobFinder {
    void triggerConcernedJob(WebHookRepresentation representation) throws BranchNotFoundException, RepositoryNotFoundException, TuleapProjectNotFoundException;
}
