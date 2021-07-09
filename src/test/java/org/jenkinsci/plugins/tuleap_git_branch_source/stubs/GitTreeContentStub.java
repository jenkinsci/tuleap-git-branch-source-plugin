package org.jenkinsci.plugins.tuleap_git_branch_source.stubs;

import io.jenkins.plugins.tuleap_api.client.GitTreeContent;

public class GitTreeContentStub implements GitTreeContent {

    private final String id;
    private final String name;
    private final String path;
    private final String type;
    private final String mode;

    public GitTreeContentStub(String id, String name, String path, String type, String mode) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.type = type;
        this.mode = mode;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public ContentType getType() {
        if(this.type.equals("dir")) {
            return ContentType.TREE;
        }

        if(this.type.equals("link")) {
            return ContentType.SYMLINK;
        }
        return ContentType.BLOB;
    }

    @Override
    public String getMode() {
        return this.mode;
    }
}
