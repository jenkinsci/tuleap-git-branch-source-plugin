package org.jenkinsci.plugins.tuleap_git_branch_source.webhook;

import hudson.Extension;
import hudson.security.csrf.CrumbExclusion;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Extension
public class TuleapWebHookCrumbExclusion extends CrumbExclusion {

    @Override
    public boolean process(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        final String path = request.getPathInfo();
        if (path != null && path.equals("/" + TuleapWebHook.WEBHOOK_URL + "/")) {
            filterChain.doFilter(request, response);
            return true;
        }
        return false;
    }
}
