package org.jenkinsci.plugins.tuleap_git_branch_source.webhook.processor;

import hudson.model.CauseAction;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapSCMNavigator;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.TuleapWebHookCause;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.BranchNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.TuleapProjectNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.RepositoryNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.model.WebHookRepresentation;

import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static jenkins.model.ParameterizedJobMixIn.*;

public class JobFinderImpl implements JobFinder {

    private static final Logger LOGGER = Logger.getLogger(JobFinderImpl.class.getName());

    private OrganizationFolderRetriever organizationFolderRetriever;

    @Inject
    public JobFinderImpl(OrganizationFolderRetriever organizationFolderRetriever) {
        this.organizationFolderRetriever = organizationFolderRetriever;
    }

    public void triggerConcernedJob(WebHookRepresentation representation) throws RepositoryNotFoundException, BranchNotFoundException, TuleapProjectNotFoundException {
        LOGGER.log(Level.FINEST, "Retrieve the concerned job...");
        Optional<OrganizationFolder> tuleapOrganizationFolder = this.organizationFolderRetriever.retrieveTuleapOrganizationFolders()
            .filter(OrganizationFolder::isSingleOrigin)
            .filter(organizationFolder -> organizationFolder.getSCMNavigators().get(0).getClass().equals(TuleapSCMNavigator.class))
            .filter(organizationFolder -> {
                TuleapSCMNavigator tuleapSCMNavigator = (TuleapSCMNavigator) organizationFolder.getSCMNavigators().get(0);
                String projectId = tuleapSCMNavigator.getprojectId();
                return representation.getTuleapProjectId().equals(projectId);
            })
            .findFirst();

        OrganizationFolder tuleapFolder = tuleapOrganizationFolder.orElseThrow(TuleapProjectNotFoundException::new);
        MultiBranchProject multiBranchProject = tuleapFolder.getJob(representation.getRepositoryName());

        if (multiBranchProject == null) {
            throw new RepositoryNotFoundException();
        }

        ParameterizedJob job = this.organizationFolderRetriever.retrieveBranchJobFromRepositoryName(multiBranchProject, representation);

        if (job == null) {
            throw new BranchNotFoundException();
        }

        if (job.scheduleBuild2(0, new CauseAction(new TuleapWebHookCause(representation)
        )) != null) {
            LOGGER.log(Level.FINEST, "The job has been successfully built");
            return;
        }
        LOGGER.log(Level.FINEST, "No job was triggered");
    }
}
