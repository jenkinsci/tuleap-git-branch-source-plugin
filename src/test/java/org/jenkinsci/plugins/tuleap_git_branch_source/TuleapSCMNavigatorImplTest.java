package org.jenkinsci.plugins.tuleap_git_branch_source;

import hudson.util.FormValidation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TuleapSCMNavigatorImplTest {

    @Test
    public void testItReturnsWarningIfThereIsNoProjectIdSet() {
        TuleapSCMNavigator.DescriptorImpl descriptorImpl = new TuleapSCMNavigator.DescriptorImpl();
        FormValidation feedbackWithEmptyString = descriptorImpl.doCheckTuleapProjectId("");
        FormValidation feedbackWithNull = descriptorImpl.doCheckTuleapProjectId(null);

        assertEquals(
            Messages.SCMNavigator_aTuleapProjectIsRequiredWarning(),
            feedbackWithEmptyString.getMessage()
        );

        assertEquals(
            FormValidation.Kind.ERROR,
            feedbackWithEmptyString.kind
        );

        assertEquals(
            Messages.SCMNavigator_aTuleapProjectIsRequiredWarning(),
            feedbackWithNull.getMessage()
        );

        assertEquals(
            FormValidation.Kind.ERROR,
            feedbackWithNull.kind
        );
    }

    @Test
    public void testItReturnsNothingIfTheProjectIdfIsSet(){
        TuleapSCMNavigator.DescriptorImpl descriptorImpl = new TuleapSCMNavigator.DescriptorImpl();

        assertEquals(
            FormValidation.ok(),
            descriptorImpl.doCheckTuleapProjectId("1")
        );
    }
}
