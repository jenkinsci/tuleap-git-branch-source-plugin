package org.jenkinsci.plugins.tuleap_git_branch_source.webhook.helper;

import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static com.google.common.base.Charsets.UTF_8;

public class TuleapWebHookHelper {
    public String getStringPayload(StaplerRequest2 request) throws IOException {
        return IOUtils.toString(request.getInputStream());
    }

    public String getUTF8DecodedPayload(String payload) throws UnsupportedEncodingException {
        return URLDecoder.decode(payload,  UTF_8.name());
    }
}
