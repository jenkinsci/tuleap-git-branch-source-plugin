package org.jenkinsci.plugins.tuleap_git_branch_source.webhook.processor;

import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import jenkins.model.ParameterizedJobMixIn.ParameterizedJob;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.model.WebHookRepresentation;

import java.util.stream.Stream;

public interface OrganizationFolderRetriever {
    Stream<OrganizationFolder> retrieveTuleapOrganizationFolders();

    ParameterizedJob retrieveBranchJobFromRepositoryName(MultiBranchProject multiBranchProject, WebHookRepresentation representation);
}
