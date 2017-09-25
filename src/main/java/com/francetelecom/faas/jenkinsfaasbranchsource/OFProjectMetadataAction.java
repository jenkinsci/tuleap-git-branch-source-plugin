package com.francetelecom.faas.jenkinsfaasbranchsource;

import com.francetelecom.faas.jenkinsfaasbranchsource.ofapi.OFProject;

import jenkins.scm.api.metadata.AvatarMetadataAction;


public class OFProjectMetadataAction extends AvatarMetadataAction {

	private final String avatar;

	public OFProjectMetadataAction(OFProject project) {
		this.avatar = project.getShortname();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAvatarDescription() {
		return Messages.OFProjectMetadataAction_IconDescription();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (this ==o ){
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		if (o instanceof OFProjectMetadataAction) {
			OFProjectMetadataAction that = (OFProjectMetadataAction) o;
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
		return "OFProjectMetadataAction {"+
				"avatar= " +avatar+
				"}";
	}
}
