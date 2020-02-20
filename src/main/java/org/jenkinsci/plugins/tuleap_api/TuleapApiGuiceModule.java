package org.jenkinsci.plugins.tuleap_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.jenkinsci.plugins.tuleap_api.internals.TuleapApiClient;
import org.jenkinsci.plugins.tuleap_api.internals.guice.ObjectMapperProvider;
import org.jenkinsci.plugins.tuleap_api.internals.guice.OkHttpClientProvider;

public class TuleapApiGuiceModule extends com.google.inject.AbstractModule {
    @Override
    protected void configure() {
        bind(OkHttpClient.class).toProvider(OkHttpClientProvider.class).asEagerSingleton();
        bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class);
        bind(AccessKeyApi.class).to(TuleapApiClient.class);
        bind(UserApi.class).to(TuleapApiClient.class);
    }
}
