package org.jenkinsci.plugins.tuleap_git_branch_source.client.impl;

import okhttp3.*;
import org.jenkinsci.plugins.tuleap_credentials.TuleapAccessToken;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

class AccessKeyInterceptor implements Interceptor {

    private final TuleapAccessToken token;

    AccessKeyInterceptor(final TuleapAccessToken token) {
        this.token = token;
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request req = chain.request();
        Request authenticatedReq = req.newBuilder().cacheControl(CacheControl.FORCE_NETWORK)
            .addHeader("Cache-Control", "no-cache").header("X-Auth-AccessKey", token.getToken().getPlainText()).build();
        return chain.proceed(authenticatedReq);
    }
}
