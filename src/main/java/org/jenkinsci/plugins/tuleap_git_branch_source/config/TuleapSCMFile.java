package org.jenkinsci.plugins.tuleap_git_branch_source.config;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.jenkins.plugins.tuleap_api.client.GitApi;
import io.jenkins.plugins.tuleap_api.client.GitTreeContent;
import io.jenkins.plugins.tuleap_api.client.internals.exceptions.InvalidTuleapResponseException;
import io.jenkins.plugins.tuleap_credentials.TuleapAccessToken;
import jenkins.scm.api.SCMFile;

public class TuleapSCMFile extends SCMFile {

    private final GitApi gitApi;
    private final String repositoryId;
    private final String ref;
    private final String path;
    private final TuleapAccessToken tuleapAccessToken;
    private boolean isDirectory;

    public TuleapSCMFile(GitApi gitApi, String repositoryId, String path, String ref, TuleapAccessToken tuleapAccessToken) {
        super();
        this.gitApi = gitApi;
        this.repositoryId = repositoryId;
        this.ref = ref;
        this.tuleapAccessToken = tuleapAccessToken;

        // TULEAP - on ne sait pas encore ce que c'est
        this.isDirectory = true;
        this.type(Type.DIRECTORY);
        this.path = path;
    }

    private TuleapSCMFile(TuleapSCMFile parent, String name, Type type) {
        super(parent, name);
        this.gitApi = parent.gitApi;
        this.repositoryId = parent.repositoryId;
        this.ref = parent.ref;
        this.isDirectory = type == Type.DIRECTORY;
        this.type(type);
    }

    @NonNull
    @Override
    protected SCMFile newChild(@NonNull String name, boolean isDirectory) {
        return new TuleapSCMFile(this, name, isDirectory);
    }

    @NonNull
    @Override
    public Iterable<SCMFile> children() throws IOException, InterruptedException {
        if (! this.isDirectory()) {
            throw new IOException("Cannot get children from a regular file");
        }

        List<GitTreeContent> treeContent = fetchTree();
        return treeContent
            .stream()
            .map( item -> {
                Type type;

                switch (item.getType()) {
                    case TREE:
                        type = Type.DIRECTORY;
                        break;
                    case BLOB:
                        type = Type.REGULAR_FILE;
                        break;
                    case SYMLINK:
                        type = Type.LINK;
                        break;
                    default:
                        type = Type.OTHER;
                }

                return new TuleapSCMFile(this, item.getName(), type);
            })
            .collect(Collectors.toList());
    }

    @Override
    public long lastModified() throws IOException, InterruptedException {
        // Unsupported
        return 0L;
    }

    @NonNull
    @Override
    protected Type type() throws IOException, InterruptedException {
        if (isDirectory) {
            return Type.DIRECTORY;
        }
        try {
            gitLabApi.getRepositoryFileApi()
                .getFile(repositoryId, getPath(), ref);
            return Type.REGULAR_FILE;
        } catch (GitLabApiException e) {
            if (e.getHttpStatus() != 404) {
                throw new IOException(e);
            }
            try {
                gitLabApi.getRepositoryApi().getTree(repositoryId, getPath(), ref);
                return Type.DIRECTORY;
            } catch (GitLabApiException ex) {
                if (e.getHttpStatus() != 404) {
                    throw new IOException(e);
                }
            }
        }
        return Type.NONEXISTENT;
    }

    @NonNull
    @Override
    public InputStream content() throws IOException, InterruptedException {
        if (this.isDirectory()) {
            throw new IOException("Cannot get raw content from a directory");
        } else {
            return fetchFile();
        }
    }

    private InputStream fetchFile() throws IOException {
        try {
            return gitLabApi.getRepositoryFileApi().getRawFile(repositoryId, ref, getPath());
        } catch (GitLabApiException e) {
            throw new IOException(String.format("%s not found at %s", getPath(), ref));
        }
    }

    private List<GitTreeContent> fetchTree() {
        return gitApi.getTree(repositoryId, ref, getPath(), tuleapAccessToken);
    }

}
