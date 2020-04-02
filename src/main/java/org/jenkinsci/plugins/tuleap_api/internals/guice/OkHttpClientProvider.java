package org.jenkinsci.plugins.tuleap_api.internals.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.jenkins.plugins.tuleap_server_configuration.TuleapConfiguration;
import jenkins.model.Jenkins;
import okhttp3.OkHttpClient;

import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class OkHttpClientProvider implements Provider<OkHttpClient> {
    private TuleapConfiguration tuleapConfiguration;

    @Inject
    public OkHttpClientProvider(TuleapConfiguration tuleapConfiguration) {
        this.tuleapConfiguration = tuleapConfiguration;
    }

    @Override
    public OkHttpClient get() {
        return new OkHttpClient
            .Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .cache(null)
            .proxy(getProxy(getTuleapHost()))
            .build();
    }

    private String getTuleapHost() {
        try {
            return new URL(tuleapConfiguration.getApiBaseUrl()).getHost();
        } catch (MalformedURLException exception) {
            throw new RuntimeException(exception);
        }
    }

    private Proxy getProxy(String host) {
        Jenkins jenkins = Jenkins.getInstanceOrNull();

        if (jenkins == null || jenkins.proxy == null) {
            return Proxy.NO_PROXY;
        } else {
            return jenkins.proxy.createProxy(host);
        }
    }
}
