package org.jenkinsci.plugins.tuleap_git_branch_source.webhook.processor;

import hudson.model.CauseAction;
import hudson.security.ACL;
import hudson.security.ACLContext;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapSCMNavigator;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.TuleapWebHookCause;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.BranchNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.TuleapProjectNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.RepositoryNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.model.WebHookRepresentation;

import java.util.logging.Level;
import java.util.logging.Logger;

import static jenkins.model.ParameterizedJobMixIn.*;

public class JobFinderImpl implements JobFinder {

    private static final Logger LOGGER = Logger.getLogger(JobFinderImpl.class.getName());

    public void triggerConcernedJob(WebHookRepresentation representation) throws BranchNotFoundException, RepositoryNotFoundException, TuleapProjectNotFoundException {
        Jenkins.get().getACL();

        try (ACLContext old = ACL.as(ACL.SYSTEM)) {

            LOGGER.log(Level.INFO, "Retrieve the concerned job...");

            for (OrganizationFolder organizationFolder : Jenkins.get().getAllItems(OrganizationFolder.class)) {
                LOGGER.log(Level.INFO, "Consider the Organization folder: {0}", organizationFolder.getName());

                if (!organizationFolder.isSingleOrigin()) {
                    LOGGER.log(Level.INFO, "This folder has no project type or has several project types");
                    continue;
                }

                if (!organizationFolder.getSCMNavigators().get(0).getClass().equals(TuleapSCMNavigator.class)) {
                    LOGGER.log(Level.INFO, "This folder is not a Tuleap Project");
                    continue;
                }

                if (representation.getTuleapProjectName().equals(organizationFolder.getName())) {

                    MultiBranchProject multiBranchProject = organizationFolder.getJob(representation.getRepositoryName());

                    if (multiBranchProject == null) {
                        throw new RepositoryNotFoundException();
                    }
                    ParameterizedJob job = (ParameterizedJob) multiBranchProject.getItem(representation.getBranchName());
                    if (job == null) {
                        throw new BranchNotFoundException();
                    }
                    if (job.scheduleBuild2(0, new CauseAction(new TuleapWebHookCause(representation)
                    )) != null) {
                        LOGGER.log(Level.INFO, "The job has been successfully built");
                        return;
                    }
                } else {
                    LOGGER.log(Level.INFO, "This Organization folder does not match");
                }
            }
            LOGGER.log(Level.INFO, "No job found!");
            throw new TuleapProjectNotFoundException();
        }
    }
}
