package org.jenkinsci.plugins.tuleap_git_branch_source.webhook.processor;

import com.google.gson.Gson;
import hudson.util.HttpResponses;
import io.jenkins.plugins.tuleap_api.client.authentication.WebhookTokenApi;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.check.TuleapWebHookChecker;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.BranchNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.RepositoryNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.RepositoryScanFailedException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.exceptions.TuleapProjectNotFoundException;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.helper.TuleapWebHookHelper;
import org.jenkinsci.plugins.tuleap_git_branch_source.webhook.model.WebHookRepresentation;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;

import static org.junit.Assert.*;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class TuleapWebHookProcessorTest {

    private Gson gson;

    private TuleapWebHookChecker checker;

    private JobFinder jobFinder;

    private TuleapWebHookHelper webHookHelper;

    @Before
    public void init() {
        this.gson = mock(Gson.class);
        this.checker = mock(TuleapWebHookChecker.class);
        this.jobFinder = mock(JobFinder.class);
        this.webHookHelper = mock(TuleapWebHookHelper.class);
    }

    @Test
    public void testItShouldReturn400WhenTheContentTypeIsNotSupported() throws IOException, TuleapProjectNotFoundException, BranchNotFoundException, RepositoryNotFoundException, RepositoryScanFailedException {

        StaplerRequest request = mock(StaplerRequest.class);
        when(request.getContentType()).thenReturn(null);
        verify(request, never()).getInputStream();

        when(this.checker.checkRequestHeaderContentType(request.getContentType())).thenReturn(false);

        TuleapWebHookProcessorImpl tuleapWebHookProcessor = new TuleapWebHookProcessorImpl(this.gson, this.checker, this.jobFinder, this.webHookHelper);
        HttpResponse expectedResponse;
        expectedResponse = HttpResponses.error(400, "Content type not supported");

        verify(this.gson, never()).fromJson("", WebHookRepresentation.class);
        verify(this.jobFinder, never()).triggerConcernedJob(any());

        HttpResponse response = tuleapWebHookProcessor.process(request);


        assertEquals(expectedResponse.toString(), response.toString());
    }

    @Test
    public void testItShouldReturn400WhenThePayloadIsEmpty() throws IOException, TuleapProjectNotFoundException, BranchNotFoundException, RepositoryNotFoundException, RepositoryScanFailedException {
        StaplerRequest request = mock(StaplerRequest.class);
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");

        when(this.checker.checkRequestHeaderContentType(request.getContentType())).thenReturn(true);
        when(this.webHookHelper.getStringPayload(request)).thenReturn("");
        when(this.webHookHelper.getUTF8DecodedPayload(anyString())).thenReturn("");

        TuleapWebHookProcessorImpl tuleapWebHookProcessor = new TuleapWebHookProcessorImpl(this.gson, this.checker, this.jobFinder, this.webHookHelper);

        HttpResponse expectedResponse;
        expectedResponse = HttpResponses.error(400, "Jenkins job cannot be triggered. The request is empty");

        HttpResponse response = tuleapWebHookProcessor.process(request);

        verify(this.gson, never()).fromJson(anyString(), any());
        verify(this.jobFinder, never()).triggerConcernedJob(any());

        assertEquals(expectedResponse.toString(), response.toString());
    }

    @Test
    public void testItShouldReturn400WhenBadFormatPayload() throws IOException, TuleapProjectNotFoundException, BranchNotFoundException, RepositoryNotFoundException, RepositoryScanFailedException {
        String payload = "{Bad format}";

        StaplerRequest request = mock(StaplerRequest.class);
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");

        when(this.checker.checkRequestHeaderContentType(request.getContentType())).thenReturn(true);
        when(this.webHookHelper.getStringPayload(request)).thenReturn(payload);
        when(this.webHookHelper.getUTF8DecodedPayload(payload)).thenReturn(payload);

        TuleapWebHookProcessorImpl tuleapWebHookProcessor = new TuleapWebHookProcessorImpl(this.gson, this.checker, this.jobFinder, this.webHookHelper);

        WebHookRepresentation representation = mock(WebHookRepresentation.class);

        when(this.gson.fromJson(payload, WebHookRepresentation.class)).thenReturn(representation);
        when(this.checker.checkPayloadContent(representation)).thenReturn(false);

        HttpResponse expectedResponse;
        expectedResponse = HttpResponses.error(400, "Bad payload format");

        HttpResponse response = tuleapWebHookProcessor.process(request);

        verify(this.jobFinder, never()).triggerConcernedJob(any());

        assertEquals(expectedResponse.toString(), response.toString());
    }

    @Test
    public void testItShouldReturn403WhenBadWhenhookToken() throws IOException, TuleapProjectNotFoundException, BranchNotFoundException, RepositoryNotFoundException, RepositoryScanFailedException {
        String payload = "{OK format}";

        StaplerRequest request = mock(StaplerRequest.class);
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");

        when(this.checker.checkRequestHeaderContentType(request.getContentType())).thenReturn(true);
        when(this.checker.checkRequestToken(any())).thenReturn(false);
        when(this.webHookHelper.getStringPayload(request)).thenReturn(payload);
        when(this.webHookHelper.getUTF8DecodedPayload(payload)).thenReturn(payload);

        TuleapWebHookProcessorImpl tuleapWebHookProcessor = new TuleapWebHookProcessorImpl(this.gson, this.checker, this.jobFinder, this.webHookHelper);

        WebHookRepresentation representation = mock(WebHookRepresentation.class);

        when(this.gson.fromJson(payload, WebHookRepresentation.class)).thenReturn(representation);
        when(this.checker.checkPayloadContent(representation)).thenReturn(true);

        HttpResponse expectedResponse;
        expectedResponse = HttpResponses.error(403, "Invalid Token");

        HttpResponse response = tuleapWebHookProcessor.process(request);

        verify(this.jobFinder, never()).triggerConcernedJob(any());

        assertEquals(expectedResponse.toString(), response.toString());
    }


    @Test
    public void testItShouldReturn200AndLogWhenTheRepositoryIsNotFound() throws IOException, TuleapProjectNotFoundException, BranchNotFoundException, RepositoryNotFoundException, RepositoryScanFailedException {
        String payload = "{Ok format}";

        StaplerRequest request = mock(StaplerRequest.class);
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");

        when(this.checker.checkRequestHeaderContentType(request.getContentType())).thenReturn(true);
        when(this.checker.checkRequestToken(any())).thenReturn(true);
        when(this.webHookHelper.getStringPayload(request)).thenReturn(payload);
        when(this.webHookHelper.getUTF8DecodedPayload(payload)).thenReturn(payload);

        TuleapWebHookProcessorImpl tuleapWebHookProcessor = new TuleapWebHookProcessorImpl(this.gson, this.checker, this.jobFinder, this.webHookHelper);

        WebHookRepresentation representation = mock(WebHookRepresentation.class);

        when(this.gson.fromJson(payload, WebHookRepresentation.class)).thenReturn(representation);
        when(this.checker.checkPayloadContent(representation)).thenReturn(true);

        doThrow(RepositoryNotFoundException.class).when(this.jobFinder).triggerConcernedJob(representation);

        HttpResponse expectedResponse;
        expectedResponse = HttpResponses.ok();

        HttpResponse response = tuleapWebHookProcessor.process(request);

        assertEquals(expectedResponse.toString(), response.toString());
    }


    @Test
    public void testItShouldReturn200AndLogWhenTheBranchIsNotFound() throws IOException, TuleapProjectNotFoundException, BranchNotFoundException, RepositoryNotFoundException, RepositoryScanFailedException {
        String payload = "{Ok format}";

        StaplerRequest request = mock(StaplerRequest.class);
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");

        when(this.checker.checkRequestHeaderContentType(request.getContentType())).thenReturn(true);
        when(this.checker.checkRequestToken(any())).thenReturn(true);
        when(this.webHookHelper.getStringPayload(request)).thenReturn(payload);
        when(this.webHookHelper.getUTF8DecodedPayload(payload)).thenReturn(payload);

        TuleapWebHookProcessorImpl tuleapWebHookProcessor = new TuleapWebHookProcessorImpl(this.gson, this.checker, this.jobFinder, this.webHookHelper);

        WebHookRepresentation representation = mock(WebHookRepresentation.class);

        when(this.gson.fromJson(payload, WebHookRepresentation.class)).thenReturn(representation);
        when(this.checker.checkPayloadContent(representation)).thenReturn(true);

        doThrow(BranchNotFoundException.class).when(this.jobFinder).triggerConcernedJob(representation);

        HttpResponse expectedResponse;
        expectedResponse = HttpResponses.ok();
        HttpResponse response = tuleapWebHookProcessor.process(request);

        assertEquals(expectedResponse.toString(), response.toString());
    }

    @Test
    public void testItShouldReturn200AndLogWhenTheTuleapProjectIsNotFound() throws IOException, TuleapProjectNotFoundException, BranchNotFoundException, RepositoryNotFoundException, RepositoryScanFailedException {
        String payload = "{Ok format}";

        StaplerRequest request = mock(StaplerRequest.class);
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");

        when(this.checker.checkRequestHeaderContentType(request.getContentType())).thenReturn(true);
        when(this.checker.checkRequestToken(any())).thenReturn(true);
        when(this.webHookHelper.getStringPayload(request)).thenReturn(payload);
        when(this.webHookHelper.getUTF8DecodedPayload(payload)).thenReturn(payload);

        TuleapWebHookProcessorImpl tuleapWebHookProcessor = new TuleapWebHookProcessorImpl(this.gson, this.checker, this.jobFinder, this.webHookHelper);

        WebHookRepresentation representation = mock(WebHookRepresentation.class);

        when(this.gson.fromJson(payload, WebHookRepresentation.class)).thenReturn(representation);
        when(this.checker.checkPayloadContent(representation)).thenReturn(true);

        doThrow(TuleapProjectNotFoundException.class).when(this.jobFinder).triggerConcernedJob(representation);

        HttpResponse expectedResponse;
        expectedResponse = HttpResponses.ok();

        HttpResponse response = tuleapWebHookProcessor.process(request);

        assertEquals(expectedResponse.toString(), response.toString());
    }

    @Test
    public void testItShouldReturn200WhenTheJobIsTriggered() throws IOException, TuleapProjectNotFoundException, BranchNotFoundException, RepositoryNotFoundException, RepositoryScanFailedException {
        String payload = "{Ok format}";

        StaplerRequest request = mock(StaplerRequest.class);
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");

        when(this.checker.checkRequestHeaderContentType(request.getContentType())).thenReturn(true);
        when(this.checker.checkRequestToken(any())).thenReturn(true);
        when(this.webHookHelper.getStringPayload(request)).thenReturn(payload);
        when(this.webHookHelper.getUTF8DecodedPayload(payload)).thenReturn(payload);

        TuleapWebHookProcessorImpl tuleapWebHookProcessor = new TuleapWebHookProcessorImpl(this.gson, this.checker, this.jobFinder, this.webHookHelper);

        WebHookRepresentation representation = mock(WebHookRepresentation.class);

        when(this.gson.fromJson(payload, WebHookRepresentation.class)).thenReturn(representation);
        when(this.checker.checkPayloadContent(representation)).thenReturn(true);
        verify(this.jobFinder, atMostOnce()).triggerConcernedJob(representation);

        HttpResponse expectedResponse;
        expectedResponse = HttpResponses.ok();

        HttpResponse response = tuleapWebHookProcessor.process(request);

        assertEquals(expectedResponse.toString(), response.toString());
    }
}
