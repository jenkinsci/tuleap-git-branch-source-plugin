package org.jenkinsci.plugins.tuleap_git_branch_source;

import hudson.util.FormValidation;

public class TuleapSCMSourceFormValidator {

    public static FormValidation doCheckProjectId(
        String projectId) {
        if (projectId.isEmpty()) {
            return FormValidation.error(Messages.TuleapSCMSource_aGitRepositoryIsRequiredError());
        }
        return FormValidation.ok();
    }

    public static FormValidation doCheckRepositoryPath(
        String repositoryPath) {
        if (repositoryPath.isEmpty()) {
            return FormValidation.error(Messages.TuleapSCMSource_aGitRepositoryIsRequiredError());
        }
        return FormValidation.ok();
    }
}
