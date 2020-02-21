package org.jenkinsci.plugins.tuleap_git_branch_source.webhook.processor;

import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.TuleapProjectNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.model.WebHookRepresentation;

import java.util.stream.Stream;

public class OrganizationFolderRetrieverImpl implements OrganizationFolderRetriever {
    @Override
    public Stream<OrganizationFolder> retrieveTuleapOrganizationFolders() throws TuleapProjectNotFoundException {
        return Jenkins.get().getAllItems(OrganizationFolder.class).stream();
    }

    @Override
    public ParameterizedJobMixIn.ParameterizedJob retrieveBranchJobFromRepositoryName(MultiBranchProject multiBranchProject, WebHookRepresentation representation){
        return (ParameterizedJobMixIn.ParameterizedJob) multiBranchProject.getItem(representation.getBranchName());
    }
}
