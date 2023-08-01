package org.jenkinsci.plugins.tuleap_git_branch_source.stubs;

import hudson.model.Actionable;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapLink;

public final class TuleapActionableStub extends Actionable {

    private final TuleapLink tuleapLink;

    private TuleapActionableStub(TuleapLink tuleapLink){
        this.tuleapLink = tuleapLink;
    }

    public static TuleapActionableStub withDefaultTuleapLink(){
        return new TuleapActionableStub(new TuleapLink("tuleap-class", "https://tuleap-git-link.example.com"));
    }
    public static TuleapActionableStub withNull(){
        return new TuleapActionableStub(null);
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getSearchUrl() {
        return null;
    }

    @Override
    public TuleapLink getAction(Class type) {
        return this.tuleapLink;
    }

}
