package org.jenkinsci.plugins.tuleap_api.internals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.util.Secret;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jenkinsci.plugins.tuleap_api.AccessKeyApi;
import org.jenkinsci.plugins.tuleap_api.AccessKeyScope;
import org.jenkinsci.plugins.tuleap_api.TuleapAuthorization;
import org.jenkinsci.plugins.tuleap_api.internals.entities.AccessKeyEntity;
import org.jenkinsci.plugins.tuleap_api.internals.exceptions.InvalidTuleapResponseException;
import org.jenkinsci.plugins.tuleap_git_branch_source.config.TuleapConfiguration;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

public class TuleapApiClient implements TuleapAuthorization, AccessKeyApi {
    private static final Logger LOGGER = Logger.getLogger(TuleapApiClient.class.getName());

    private OkHttpClient client;

    private TuleapConfiguration tuleapConfiguration;

    private ObjectMapper objectMapper;

    @Inject
    public TuleapApiClient(
        TuleapConfiguration tuleapConfiguration,
        OkHttpClient client,
        ObjectMapper objectMapper
    ) {
        this.tuleapConfiguration = tuleapConfiguration;
        this.client = client;
        this.objectMapper = objectMapper;
    }

    @Override
    public Boolean checkAccessKeyIsValid(Secret secret) {
        Request request = new Request.Builder()
            .url(tuleapConfiguration.getApiBaseUrl() + this.ACCESS_KEY_API + this.SELF_ID)
            .header(this.AUTHORIZATION_HEADER, secret.getPlainText())
            .get()
            .build();

        try (Response response = client.newCall(request).execute()) {
            return response.code() == 200;
        } catch (IOException exception) {
            return false;
        }
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE") // see https://github.com/spotbugs/spotbugs/issues/651
    public ImmutableList<AccessKeyScope> getAccessKeyScopes(Secret secret) {
        Request request = new Request.Builder()
            .url(tuleapConfiguration.getApiBaseUrl() + this.ACCESS_KEY_API + this.SELF_ID)
            .header(this.AUTHORIZATION_HEADER, secret.getPlainText())
            .get()
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (! response.isSuccessful()) {
                throw new InvalidTuleapResponseException(response);
            }

            return ImmutableList.copyOf(
                objectMapper
                .readValue(Objects.requireNonNull(response.body()).string(), AccessKeyEntity.class)
                .getScopes()
            );
        } catch (IOException | InvalidTuleapResponseException exception) {
            LOGGER.severe(exception.getMessage());
            return ImmutableList.of();
        }
    }
}
