package org.jenkinsci.plugins.tuleap_git_branch_source.helpers;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.jenkins.plugins.tuleap_api.client.GitApi;
import io.jenkins.plugins.tuleap_api.client.ProjectApi;
import io.jenkins.plugins.tuleap_api.client.PullRequestApi;
import io.jenkins.plugins.tuleap_api.client.TuleapApiGuiceModule;

public class TuleapApiRetriever {


    private static Injector getApiGuiceModule() {
        return Guice.createInjector(new TuleapApiGuiceModule());
    }

    public static GitApi getGitApi() {
        return getApiGuiceModule().getInstance(GitApi.class);
    }

    public static PullRequestApi getPullRequestApi() {
        return getApiGuiceModule().getInstance(PullRequestApi.class);
    }

    public static ProjectApi getProjectApi() {
        return getApiGuiceModule().getInstance(ProjectApi.class);
    }
}
