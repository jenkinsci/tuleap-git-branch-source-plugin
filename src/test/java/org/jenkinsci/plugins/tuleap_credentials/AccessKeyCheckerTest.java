package org.jenkinsci.plugins.tuleap_credentials;

import com.google.common.collect.ImmutableList;
import org.jenkinsci.plugins.tuleap_api.AccessKeyApi;
import org.jenkinsci.plugins.tuleap_api.AccessKeyScope;
import org.jenkinsci.plugins.tuleap_credentials.exceptions.InvalidAccessKeyException;
import org.jenkinsci.plugins.tuleap_credentials.exceptions.InvalidScopesForAccessKeyException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccessKeyCheckerTest {
    @Mock private AccessKeyApi client;
    @InjectMocks private AccessKeyChecker accessKeyChecker;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = InvalidAccessKeyException.class)
    public void itShouldThrowAnExceptionIfKeyIsInvalid() throws InvalidScopesForAccessKeyException, InvalidAccessKeyException {
        final String accessKey = "SomeAccessKey";
        when(client.checkAccessKeyIsValid(accessKey)).thenReturn(false);

        accessKeyChecker.verifyAccessKey(accessKey);
    }

    @Test(expected = InvalidScopesForAccessKeyException.class)
    public void itShouldThrowAnExceptionIfKeyDoesNotHaveTheNeededScopes() throws InvalidScopesForAccessKeyException, InvalidAccessKeyException {
        final String accessKey = "SomeAccessKey";
        final AccessKeyScope scope = mock(AccessKeyScope.class);

        when(client.checkAccessKeyIsValid(accessKey)).thenReturn(true);
        when(scope.getIdentifier()).thenReturn("some:scope");
        when(client.getAccessKeyScopes(accessKey)).thenReturn(ImmutableList.of(scope));

        accessKeyChecker.verifyAccessKey(accessKey);
    }

    @Test
    public void itShouldNotGenerateAnyExceptionIfAccessKeyIsValid() throws InvalidScopesForAccessKeyException, InvalidAccessKeyException {
        final String accessKey = "SomeAccessKey";
        final AccessKeyScope scope1 = mock(AccessKeyScope.class);
        final AccessKeyScope scope2 = mock(AccessKeyScope.class);

        when(client.checkAccessKeyIsValid(accessKey)).thenReturn(true);
        when(scope1.getIdentifier()).thenReturn("write:rest");
        when(scope2.getIdentifier()).thenReturn("write:git_repository");
        when(client.getAccessKeyScopes(accessKey)).thenReturn(ImmutableList.of(scope1, scope2));

        accessKeyChecker.verifyAccessKey(accessKey);
    }
}
