package org.jenkinsci.plugins.tuleap_api.internals;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.tuleap_git_branch_source.config.TuleapConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class TuleapApiClientTest {
    private TuleapConfiguration tuleapConfiguration;
    private OkHttpClient client;
    private ObjectMapper mapper;
    private TuleapApiClient tuleapApiClient;

    @Before
    public void setUp() {
        client = mock(OkHttpClient.class);
        tuleapConfiguration = mock(TuleapConfiguration.class);
        mapper = new ObjectMapper().registerModule(new GuavaModule());
        tuleapApiClient = new TuleapApiClient(tuleapConfiguration, client, mapper);

        when(tuleapConfiguration.getApiBaseUrl()).thenReturn("https://example.tuleap.test");
    }

    @Test
    public void itShouldReturnFalseIfTuleapServerDoesNotAnswer200() throws IOException {
        Call call = mock(Call.class);
        Response response = mock(Response.class);

        when(client.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.code()).thenReturn(400);

        assertFalse(tuleapApiClient.checkAccessKeyIsValid("whatever"));
    }

    @Test
    public void itShouldReturnTrueIfTuleapServerAnswers200() throws IOException {
        Call call = mock(Call.class);
        Response response = mock(Response.class);

        when(client.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.code()).thenReturn(200);

        assertTrue(tuleapApiClient.checkAccessKeyIsValid("whatever"));
    }

    @Test
    public void itShouldReturnAnEmptyScopesListIfCallIsNotSuccessfull() throws IOException {
        Call call = mock(Call.class);
        Response response = mock(Response.class);

        when(client.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.isSuccessful()).thenReturn(false);

        assertEquals(0, tuleapApiClient.getAccessKeyScopes("whatever").size());
    }

    @Test
    public void itShouldReturnScopesFromTuleapServerResponse() throws IOException {
        Call call = mock(Call.class);
        Response response = mock(Response.class);
        ResponseBody responseBody = mock(ResponseBody.class);
        String json_payload = IOUtils.toString(TuleapApiClientTest.class.getResourceAsStream("access_key_payload.json"));

        when(client.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn(json_payload);

        assertEquals(2, tuleapApiClient.getAccessKeyScopes("whatever").size());
    }
}
