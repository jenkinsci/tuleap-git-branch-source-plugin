package org.jenkinsci.plugins.tuleap_git_branch_source.webhook;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TuleapWebHookCrumbExclusionTest {

    @Test
    public void testItReturnsFalseWhenTheURLIsNotWhiteListed() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getPathInfo()).thenReturn("/amg/");
        verify(filterChain, never()).doFilter(request, response);

        TuleapWebHookCrumbExclusion crumbExclusion = new TuleapWebHookCrumbExclusion();

        assertFalse(crumbExclusion.process(request, response, filterChain));
    }

    @Test
    public void testItReturnsTrueWhenTheURLIsWhiteListed() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getPathInfo()).thenReturn("/tuleap-hook/");
        verify(filterChain, atMostOnce()).doFilter(request, response);

        TuleapWebHookCrumbExclusion crumbExclusion = new TuleapWebHookCrumbExclusion();

        assertTrue(crumbExclusion.process(request, response, filterChain));
    }
}
