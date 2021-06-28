package org.jenkinsci.plugins.tuleap_git_branch_source.stubs;

import io.jenkins.plugins.tuleap_api.client.GitFileContent;

public class GitFileContentStub implements GitFileContent {

    private String content;

    public GitFileContentStub(String content){
        this.content = content;
    }
    @Override
    public String getEncoding() {
        return "base64";
    }

    @Override
    public Integer getSize() {
        return 10;
    }

    @Override
    public String getName() {
        return "faboulousous";
    }

    @Override
    public String getPath() {
        return "directory/faboulousous";
    }

    @Override
    public String getContent() {
        return this.content;
    }
}
