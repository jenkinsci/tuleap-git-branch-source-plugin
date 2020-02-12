package org.jenkinsci.plugins.tuleap_git_branch_source.webhook;

import hudson.Extension;
import hudson.security.csrf.CrumbExclusion;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
