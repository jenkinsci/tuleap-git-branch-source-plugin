package org.jenkinsci.plugins.tuleap_git_branch_source.webhook.check;

import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.model.WebHookRepresentation;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TuleapWebHookCheckerTest {

    @Test
    public void testItReturnsTrueIfTheContentTypeIsCorrect() {
        TuleapWebHookChecker checker = new TuleapWebHookCheckerImpl();
        assertTrue(checker.checkRequestHeaderContentType("application/x-www-form-urlencoded"));
    }

    @Test
    public void testItReturnsFalseIfThereIsNoContentType() {
        TuleapWebHookChecker checker = new TuleapWebHookCheckerImpl();
        assertFalse(checker.checkRequestHeaderContentType(null));
    }

    @Test
    public void testItReturnsFalseIfTheContentTypeIsIncorrect() {
        TuleapWebHookChecker checker = new TuleapWebHookCheckerImpl();
        assertFalse(checker.checkRequestHeaderContentType("application/zoe"));
    }

    @Test
    public void testItReturnsTrueIfThePayloadIsCorrect() {
        TuleapWebHookChecker checker = new TuleapWebHookCheckerImpl();
        WebHookRepresentation representation = mock(WebHookRepresentation.class);
        when(representation.getTuleapProjectName()).thenReturn("Aufrecht-Melcher-Großaspach");
        when(representation.getRepositoryName()).thenReturn("W204");
        when(representation.getBranchName()).thenReturn("C63");
        assertTrue(checker.checkPayloadContent(representation));
    }

    @Test
    public void testItReturnsFalseIfThePayloadIsNotCorrect() {
        TuleapWebHookChecker checker = new TuleapWebHookCheckerImpl();
        WebHookRepresentation representation = mock(WebHookRepresentation.class);
        when(representation.getTuleapProjectName()).thenReturn(null);
        when(representation.getRepositoryName()).thenReturn("C63");
        when(representation.getBranchName()).thenReturn(null);
        assertFalse(checker.checkPayloadContent(representation));
    }


}
