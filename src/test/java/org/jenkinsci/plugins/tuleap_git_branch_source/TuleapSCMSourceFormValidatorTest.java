package org.jenkinsci.plugins.tuleap_git_branch_source;


import hudson.util.FormValidation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TuleapSCMSourceFormValidatorTest {

    @Test
    public void testTheFormValidationIsInErrorIfTheProjectIsEmpty() {
        FormValidation result = TuleapSCMSourceFormValidator.doCheckProjectId("");
        assertEquals(
            FormValidation.Kind.ERROR,
            result.kind
        );
    }

    @Test
    public void testTheFormValidationIsOkIfTheProjectIsGiven() {
        FormValidation result = TuleapSCMSourceFormValidator.doCheckProjectId("152");
        assertEquals(
            FormValidation.Kind.OK,
            result.kind
        );
    }

    @Test
    public void testTheFormValidationIsOkIfTheRepositoryPathIsEmpty() {
        FormValidation result = TuleapSCMSourceFormValidator.doCheckRepositoryPath("");
        assertEquals(
            FormValidation.Kind.ERROR,
            result.kind
        );
    }

    @Test
    public void testTheFormValidationIsOkIfTheRepositoryPathIsGiven() {
        FormValidation result = TuleapSCMSourceFormValidator.doCheckProjectId("project/repo.git");
        assertEquals(
            FormValidation.Kind.OK,
            result.kind
        );
    }
}
