package org.jenkinsci.plugins.tuleap_branch_source.client.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


import okhttp3.CacheControl;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * An alternative OKHttp mechanism to add authentication. Other alternative is via .authenticator() method
 */
class BasicAuthInterceptor implements Interceptor {

    private String credentials;

    BasicAuthInterceptor(final String login, final String password) {
        this.credentials = Credentials.basic(login, password, StandardCharsets.UTF_8);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request req = chain.request();
        Request authenticatedReq = req.newBuilder().cacheControl(CacheControl.FORCE_NETWORK)
            .addHeader("Cache-Control", "no-cache").header("Authorization", credentials).build();
        return chain.proceed(authenticatedReq);
    }
}
