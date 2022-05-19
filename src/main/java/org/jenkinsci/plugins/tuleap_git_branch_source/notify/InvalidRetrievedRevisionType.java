package org.jenkinsci.plugins.tuleap_git_branch_source.notify;

public class InvalidRetrievedRevisionType extends RuntimeException {
    public InvalidRetrievedRevisionType() {
        super("Given revision is not from a Tuleap revision");
    }
}
