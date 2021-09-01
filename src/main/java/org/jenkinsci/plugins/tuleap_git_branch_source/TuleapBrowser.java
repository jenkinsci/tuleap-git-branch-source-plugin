package org.jenkinsci.plugins.tuleap_git_branch_source;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.browser.GitRepositoryBrowser;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class TuleapBrowser extends GitRepositoryBrowser {

    @DataBoundConstructor
    public TuleapBrowser(String repositoryUrl) {
        super(repositoryUrl);
    }

    public String getRepositoryUrl() {
        return super.getRepoUrl();
    }

    @Override
    public URL getDiffLink(GitChangeSet.Path path) throws IOException {
        if (path.getEditType() != EditType.EDIT || path.getSrc() == null || path.getDst() == null
            || path.getChangeSet().getParentCommit() == null) {
            return null;
        }
        return encodeURL(new URL(this.getRepositoryUrl() + "?a=commitdiff&h=" + path.getChangeSet().getId()));
    }

    @Override
    public URL getFileLink(GitChangeSet.Path path) throws IOException, URISyntaxException {
        if (path.getEditType().equals(EditType.DELETE)) {
            return null;
        }
        URL fileUrl = new URL(this.getRepositoryUrl() + "?a=blob&hb=" + path.getChangeSet().getId() + "&f=" + path.getPath());
        return encodeURL(fileUrl);
    }

    @Override
    public URL getChangeSetLink(GitChangeSet changeSet) throws IOException {
        return encodeURL(new URL(this.getRepositoryUrl() + "?a=commit&h=" + changeSet.getId()));
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {

        @NonNull
        public String getDisplayName() {
            return "Tuleap";
        }

        @Override
        public TuleapBrowser newInstance(StaplerRequest req, @NonNull JSONObject jsonObject)
            throws FormException {
            return req.bindJSON(TuleapBrowser.class, jsonObject);
        }
    }
}
