package org.jenkinsci.plugins.tuleap_git_branch_source;

import io.jenkins.plugins.tuleap_api.deprecated_client.api.TuleapProject;
import jenkins.scm.api.metadata.AvatarMetadataAction;

import java.util.Objects;

public class TuleapProjectMetadataAction extends AvatarMetadataAction {

    private final String avatar;

    public TuleapProjectMetadataAction(TuleapProject project) {
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

        TuleapProjectMetadataAction that = (TuleapProjectMetadataAction) o;
        return Objects.equals(avatar, that.avatar);
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
