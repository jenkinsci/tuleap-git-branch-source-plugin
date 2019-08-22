package org.jenkinsci.plugins.tuleap_git_branch_source;

import org.apache.commons.jelly.JellyContext;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.jenkins.ui.icon.IconSpec;
import org.kohsuke.stapler.Stapler;

import hudson.model.Action;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;

public class TuleapLink implements Action, IconSpec {
    /**
     * The icon class name to use.
     */
    @Nonnull
    private final String iconClassName;

    /**
     * Target of the hyperlink to take the user to.
     */
    @Nonnull
    private final String url;

    public TuleapLink(@Nonnull String iconClassName, @Nonnull String url) {
        this.iconClassName = iconClassName;
        this.url = url;
    }

    @Override
    public String getIconFileName() {
        String iconClassName = getIconClassName();
        if (iconClassName != null) {
            Icon icon = IconSet.icons.getIconByClassSpec(iconClassName + " icon-md");
            if (icon != null) {
                JellyContext ctx = new JellyContext();
                ctx.setVariable("resURL", Stapler.getCurrentRequest().getContextPath() + Jenkins.RESOURCE_PATH);
                return icon.getQualifiedUrl(ctx);
            }
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Tuleap Project";
    }

    @Override
    public String getUrlName() {
        return url;
    }

    @Override
    public String getIconClassName() {
        return iconClassName;
    }

    @Nonnull
    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TuleapLink that = (TuleapLink) o;

        if (!iconClassName.equals(that.iconClassName)) {
            return false;
        }
        return url.equals(that.url);
    }

    @Override
    public int hashCode() {
        int result = iconClassName.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }
}
