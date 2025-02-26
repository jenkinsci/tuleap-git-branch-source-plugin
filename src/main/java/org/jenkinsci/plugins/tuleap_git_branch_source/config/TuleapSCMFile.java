package org.jenkinsci.plugins.tuleap_git_branch_source.config;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import io.jenkins.plugins.tuleap_api.client.GitApi;
import io.jenkins.plugins.tuleap_api.client.GitFileContent;
import io.jenkins.plugins.tuleap_api.client.GitTreeContent;
import io.jenkins.plugins.tuleap_api.client.exceptions.git.FileContentNotFoundException;
import io.jenkins.plugins.tuleap_api.client.exceptions.git.TreeNotFoundException;
import io.jenkins.plugins.tuleap_credentials.TuleapAccessToken;
import jenkins.scm.api.SCMFile;

public class TuleapSCMFile extends SCMFile {

    private final GitApi gitApi;
    private final String repositoryId;
    private final String ref;
    private final TuleapAccessToken tuleapAccessToken;
    private final boolean isDirectory;

    public TuleapSCMFile(GitApi gitApi, String repositoryId, String ref, TuleapAccessToken tuleapAccessToken) {
        super();
        this.gitApi = gitApi;
        this.repositoryId = repositoryId;
        this.ref = ref;
        this.tuleapAccessToken = tuleapAccessToken;

        this.isDirectory = true;
        this.type(Type.DIRECTORY);
    }

    private TuleapSCMFile(TuleapSCMFile parent, String name, Type type) {
        super(parent, name);
        this.gitApi = parent.gitApi;
        this.repositoryId = parent.repositoryId;
        this.tuleapAccessToken = parent.tuleapAccessToken;
        this.ref = parent.ref;
        this.isDirectory = type == Type.DIRECTORY;
        this.type(type);
    }

    private TuleapSCMFile(TuleapSCMFile parent, String name, Boolean isDirectory) {
        super(parent, name);
        this.gitApi = parent.gitApi;
        this.tuleapAccessToken = parent.tuleapAccessToken;
        this.repositoryId = parent.repositoryId;
        this.ref = parent.ref;
        this.isDirectory = isDirectory;
    }


    @NonNull
    @Override
    protected SCMFile newChild(@NonNull String name, boolean isDirectory) {
        return new TuleapSCMFile(this, name, isDirectory);
    }

    @NonNull
    @Override
    public Iterable<SCMFile> children() throws IOException, InterruptedException {
        if (!this.isDirectory()) {
            throw new IOException("Cannot get children from a regular file");
        }

        List<GitTreeContent> treeContent;
        try {
            treeContent = this.fetchTree();
        } catch (TreeNotFoundException e) {
            throw new IOException("Tree content cannot be retrieved: " + e.getMessage());
        }
        return treeContent
            .stream()
            .map(item -> {
                Type type = switch (item.getType()) {
                    case TREE -> Type.DIRECTORY;
                    case BLOB -> Type.REGULAR_FILE;
                    case SYMLINK -> Type.LINK;
                    default -> Type.OTHER;
                };

                return new TuleapSCMFile(this, item.getName(), type);
            })
            .collect(Collectors.toList());
    }

    @Override
    public long lastModified() {
        // Unsupported
        return 0L;
    }

    @NonNull
    @Override
    protected Type type() {
        if (isDirectory) {
            return Type.DIRECTORY;
        }
        try {
            this.gitApi.getFileContent(this.repositoryId, this.ref, this.getPath(), this.tuleapAccessToken);
            return Type.REGULAR_FILE;
        } catch (FileContentNotFoundException e) {
            try {
                this.gitApi.getTree(repositoryId, ref, this.getPath(), tuleapAccessToken);
                return Type.DIRECTORY;
            } catch (TreeNotFoundException treeNotFoundException) {
                return Type.NONEXISTENT;
            }
        }
    }

    @NonNull
    @Override
    public InputStream content() throws IOException, InterruptedException {
        if (this.isDirectory()) {
            throw new IOException("Cannot get raw content from a directory");
        }
        try {
            byte[] decodedByteContent = Base64.getDecoder().decode(this.fetchFile().getContent());
            return new ByteArrayInputStream(decodedByteContent);
        } catch (FileContentNotFoundException e) {
            throw new IOException("File not found: " + e.getMessage());
        }
    }

    private List<GitTreeContent> fetchTree() throws TreeNotFoundException {
        return gitApi.getTree(repositoryId, ref, this.getPath(), tuleapAccessToken);
    }

    private GitFileContent fetchFile() throws FileContentNotFoundException {
        return this.gitApi.getFileContent(this.repositoryId, this.ref, this.getPath(), this.tuleapAccessToken);
    }

}
