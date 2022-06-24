package org.jenkinsci.plugins.tuleap_git_branch_source;

import io.jenkins.plugins.tuleap_api.client.Project;
import jenkins.scm.api.metadata.AvatarMetadataAction;

public class TuleapProjectMetadataAction extends AvatarMetadataAction {

    private final String avatar;

    public TuleapProjectMetadataAction(Project project) {
        this.avatar = project.getShortname();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAvatarDescription() {
        return Messages.ProjectMetadataAction_iconDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        if (o instanceof TuleapProjectMetadataAction) {
            TuleapProjectMetadataAction that = (TuleapProjectMetadataAction) o;
            return avatar != null ? avatar.equals(that.avatar) : that.avatar == null;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (avatar != null ? avatar.hashCode() : 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "OFProjectMetadataAction {" + "avatar= " + avatar + "}";
    }
}
