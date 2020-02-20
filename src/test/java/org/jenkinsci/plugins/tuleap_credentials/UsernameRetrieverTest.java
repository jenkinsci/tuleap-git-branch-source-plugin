package org.jenkinsci.plugins.tuleap_credentials;

import hudson.util.Secret;
import org.jenkinsci.plugins.tuleap_api.User;
import org.jenkinsci.plugins.tuleap_api.UserApi;
import org.jenkinsci.plugins.tuleap_api.internals.entities.UserEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsernameRetrieverTest {
    @Mock
    private UserApi client;

    @InjectMocks
    private UsernameRetriever usernameRetriever;

    private Secret secret;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        secret = mock(Secret.class);
        when(secret.getPlainText()).thenReturn("SomeAccessKey");
    }

    @Test
    public void itShouldReturnTheUsersUsername() {
        final String username = "mjagger";
        final User user = new UserEntity(username);

        when(client.getUserForAccessKey(secret)).thenReturn(user);

        assertEquals(username, usernameRetriever.getUsernameForToken(secret));
    }
}
