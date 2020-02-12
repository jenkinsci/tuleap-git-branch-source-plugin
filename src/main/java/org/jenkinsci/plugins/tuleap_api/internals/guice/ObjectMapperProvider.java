package org.jenkinsci.plugins.tuleap_api.internals.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.inject.Provider;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class ObjectMapperProvider implements Provider<ObjectMapper> {

    @Override
    public ObjectMapper get() {
        return new ObjectMapper().registerModule(new GuavaModule());
    }
}
