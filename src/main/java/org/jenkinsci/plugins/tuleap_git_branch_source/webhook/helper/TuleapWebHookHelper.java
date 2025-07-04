package org.jenkinsci.plugins.tuleap_git_branch_source.webhook.helper;

import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest2;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;


public class TuleapWebHookHelper {
    public String getStringPayload(StaplerRequest2 request) throws IOException {
        return IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
    }

    public String getUTF8DecodedPayload(String payload) {
        return URLDecoder.decode(payload, StandardCharsets.UTF_8);
    }
}
