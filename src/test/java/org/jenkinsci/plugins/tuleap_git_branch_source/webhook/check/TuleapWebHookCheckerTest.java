package org.jenkinsci.plugins.tuleap_git_branch_source.webhook.check;

import io.jenkins.plugins.tuleap_api.client.authentication.WebhookTokenApi;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.model.WebHookRepresentation;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TuleapWebHookCheckerTest {

    @Test
    public void testItReturnsTrueIfTheContentTypeIsCorrect() {
        TuleapWebHookChecker checker = new TuleapWebHookCheckerImpl(mock(WebhookTokenApi.class));
        assertTrue(checker.checkRequestHeaderContentType("application/x-www-form-urlencoded"));
    }

    @Test
    public void testItReturnsFalseIfThereIsNoContentType() {
        TuleapWebHookChecker checker = new TuleapWebHookCheckerImpl(mock(WebhookTokenApi.class));
        assertFalse(checker.checkRequestHeaderContentType(null));
    }

    @Test
    public void testItReturnsFalseIfTheContentTypeIsIncorrect() {
        TuleapWebHookChecker checker = new TuleapWebHookCheckerImpl(mock(WebhookTokenApi.class));
        assertFalse(checker.checkRequestHeaderContentType("application/zoe"));
    }

    @Test
    public void testItReturnsTrueIfThePayloadIsCorrect() {
        TuleapWebHookChecker checker = new TuleapWebHookCheckerImpl(mock(WebhookTokenApi.class));
        WebHookRepresentation representation = mock(WebHookRepresentation.class);
        when(representation.getTuleapProjectId()).thenReturn("200");
        when(representation.getRepositoryName()).thenReturn("W204");
        when(representation.getBranchName()).thenReturn("C63");
        when(representation.getToken()).thenReturn("aToken");
        assertTrue(checker.checkPayloadContent(representation));
    }

    @Test
    public void testItReturnsFalseIfThePayloadIsNotCorrect() {
        TuleapWebHookChecker checker = new TuleapWebHookCheckerImpl(mock(WebhookTokenApi.class));
        WebHookRepresentation representation = mock(WebHookRepresentation.class);
        when(representation.getTuleapProjectId()).thenReturn(null);
        when(representation.getRepositoryName()).thenReturn("C63");
        when(representation.getBranchName()).thenReturn(null);
        assertFalse(checker.checkPayloadContent(representation));
    }

    @Test
    public void testItReturnsFalseIfTheWebhookTokenIsNotCorrect() {
        WebhookTokenApi api = mock(WebhookTokenApi.class);
        TuleapWebHookChecker checker = new TuleapWebHookCheckerImpl(api);
        WebHookRepresentation representation = mock(WebHookRepresentation.class);
        when(api.checkWebhookTokenIsValid(any())).thenReturn(false);

        assertFalse(checker.checkRequestToken(representation));
    }

    @Test
    public void testItReturnsTrueIfTheWebhookTokenIsNotCorrect() {
        WebhookTokenApi api = mock(WebhookTokenApi.class);
        TuleapWebHookChecker checker = new TuleapWebHookCheckerImpl(api);
        WebHookRepresentation representation = mock(WebHookRepresentation.class);
        when(api.checkWebhookTokenIsValid(any())).thenReturn(true);

        assertTrue(checker.checkRequestToken(representation));
    }
}
