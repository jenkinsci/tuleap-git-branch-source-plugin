package org.jenkinsci.plugins.tuleap_git_branch_source.stubs;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import hudson.util.Secret;
import io.jenkins.plugins.tuleap_credentials.TuleapAccessToken;
import io.jenkins.plugins.tuleap_credentials.TuleapAccessTokenImpl;
import org.jetbrains.annotations.NotNull;

public class TuleapAccessTokenStub implements TuleapAccessToken {
    @NotNull
    @Override
    public Secret getToken() {
        return Secret.fromString("my_t0k3n");
    }

    @NotNull
    @Override
    public Secret getPassword() {
        return Secret.fromString("d0lph1n");
    }

    @NotNull
    @Override
    public String getDescription() {
        return "";
    }

    @NotNull
    @Override
    public String getId() {
        return "";
    }

    @NotNull
    @Override
    public String getUsername() {
        return "Coco";
    }

    @Override
    public CredentialsScope getScope() {
        return CredentialsScope.SYSTEM;
    }

    @NotNull
    @Override
    public CredentialsDescriptor getDescriptor() {
        return new TuleapAccessTokenImpl.DescriptorImpl();
    }
}
