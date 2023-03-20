package org.jenkinsci.plugins.tuleap_git_branch_source.config;

import com.google.common.collect.ImmutableList;
import io.jenkins.plugins.tuleap_api.client.*;
import io.jenkins.plugins.tuleap_api.client.exceptions.git.FileContentNotFoundException;
import io.jenkins.plugins.tuleap_api.client.exceptions.git.TreeNotFoundException;
import io.jenkins.plugins.tuleap_api.client.internals.entities.TuleapBuildStatus;
import io.jenkins.plugins.tuleap_credentials.TuleapAccessToken;
import jenkins.scm.api.SCMFile;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.tuleap_git_branch_source.stubs.GitFileContentStub;
import org.jenkinsci.plugins.tuleap_git_branch_source.stubs.GitTreeContentStub;
import org.jenkinsci.plugins.tuleap_git_branch_source.stubs.TuleapAccessTokenStub;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;

import static org.junit.Assert.*;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TuleapSCMFileTest {

    @Test(expected = IOException.class)
    public void iThrowsAnExceptionWhenWeWantToRetrieveTheDirectoryContentWhenTheSCMFileIsAFile() throws IOException, InterruptedException {
        GitApi gitApi = new GitApi() {
            @Override
            public void sendBuildStatus(String s, String s1, TuleapBuildStatus tuleapBuildStatus, StringCredentials stringCredentials) {
                //do nothing
            }

            @Override
            public void sendBuildStatus(String s, String s1, TuleapBuildStatus tuleapBuildStatus, TuleapAccessToken tuleapAccessToken) {
                //do nothing
            }

            @Override
            public GitCommit getCommit(String s, String s1, TuleapAccessToken tuleapAccessToken) {
                return null;
            }

            @Override
            public List<GitTreeContent> getTree(String s, String s1, String s2, TuleapAccessToken tuleapAccessToken) throws TreeNotFoundException {
                return null;
            }

            @Override
            public GitFileContent getFileContent(String s, String s1, String s2, TuleapAccessToken tuleapAccessToken) throws FileContentNotFoundException {
                return null;
            }

            @Override
            public List<GitPullRequest> getPullRequests(String repositoryId, TuleapAccessToken token) {
                return null;
            }

            @Override
            public List<GitBranch> getBranches(String repositoryId, TuleapAccessToken token) {
                return null;
            }
        };
        TuleapSCMFile scmRootDirectory = new TuleapSCMFile(gitApi, "4", "master", new TuleapAccessTokenStub());
        TuleapSCMFile scmFile = (TuleapSCMFile) scmRootDirectory.newChild("whatever", false);

        scmFile.children();
    }

    @Test(expected = IOException.class)
    public void itThrowsAnExceptionIfTheDirectoryIsNotFoundFromTuleap() throws IOException, InterruptedException {
        GitApi gitApi = new GitApi() {
            @Override
            public void sendBuildStatus(String s, String s1, TuleapBuildStatus tuleapBuildStatus, StringCredentials stringCredentials) {
                //do nothing
            }

            @Override
            public void sendBuildStatus(String s, String s1, TuleapBuildStatus tuleapBuildStatus, TuleapAccessToken tuleapAccessToken) {
                //do nothing
            }

            @Override
            public GitCommit getCommit(String s, String s1, TuleapAccessToken tuleapAccessToken) {
                return null;
            }

            @Override
            public List<GitTreeContent> getTree(String s, String s1, String s2, TuleapAccessToken tuleapAccessToken) throws TreeNotFoundException {
                throw new TreeNotFoundException();
            }

            @Override
            public GitFileContent getFileContent(String s, String s1, String s2, TuleapAccessToken tuleapAccessToken) throws FileContentNotFoundException {
                return null;
            }

            @Override
            public List<GitPullRequest> getPullRequests(String repositoryId, TuleapAccessToken token) {
                return null;
            }

            @Override
            public List<GitBranch> getBranches(String repositoryId, TuleapAccessToken token) {
                return null;
            }
        };
        TuleapSCMFile tuleapSCMFile = new TuleapSCMFile(gitApi, "4", "master", new TuleapAccessTokenStub());
        tuleapSCMFile.children();
    }

    @Test
    public void itReturnsTheContentOfTheWantedDirectory() throws IOException, InterruptedException {
        GitTreeContent file1 = new GitTreeContentStub("1", "tchiki tchiki", "tchiki tchiki", "file", "100644");
        GitTreeContent file2 = new GitTreeContentStub("2", "naha", "naha", "file", "100644");
        GitTreeContent tree = new GitTreeContentStub("3", "the world chico", "the world chico", "dir", "040000");
        GitTreeContent symlink = new GitTreeContentStub("4", "linkz", "linkz", "link", "120000");

        GitApi gitApi = new GitApi() {

            @Override
            public void sendBuildStatus(String s, String s1, TuleapBuildStatus tuleapBuildStatus, StringCredentials stringCredentials) {
                //do nothing
            }

            @Override
            public void sendBuildStatus(String s, String s1, TuleapBuildStatus tuleapBuildStatus, TuleapAccessToken tuleapAccessToken) {
                //do nothing
            }

            @Override
            public GitCommit getCommit(String s, String s1, TuleapAccessToken tuleapAccessToken) {
                return null;
            }

            @Override
            public List<GitTreeContent> getTree(String s, String s1, String s2, TuleapAccessToken tuleapAccessToken) throws TreeNotFoundException {
                return ImmutableList.of(file1, file2, tree, symlink);
            }

            @Override
            public GitFileContent getFileContent(String s, String s1, String s2, TuleapAccessToken tuleapAccessToken) throws FileContentNotFoundException {
                return null;
            }

            @Override
            public List<GitPullRequest> getPullRequests(String repositoryId, TuleapAccessToken token) {
                return null;
            }

            @Override
            public List<GitBranch> getBranches(String repositoryId, TuleapAccessToken token) {
                return null;
            }
        };
        TuleapSCMFile tuleapSCMFile = new TuleapSCMFile(gitApi, "4", "master", new TuleapAccessTokenStub());

        List<GitTreeContent> expectedChildren = ImmutableList.of(file1, file2, tree, symlink);
        Iterable<SCMFile> children = tuleapSCMFile.children();

        children.forEach(scmFile -> {

            GitTreeContent expectedContent = expectedChildren.stream()
                .filter(currentContent -> scmFile.getName().equals(currentContent.getName()))
                .findFirst()
                .orElseThrow(RuntimeException::new);

            assertEquals(expectedContent.getName(), scmFile.getName());
            assertEquals(expectedContent.getPath(), scmFile.getPath());
        });
    }

    @Test
    public void itReturnsTheDecodedContentOfAFile() throws IOException, InterruptedException {
        GitApi gitApi = new GitApi() {

            @Override
            public void sendBuildStatus(String s, String s1, TuleapBuildStatus tuleapBuildStatus, StringCredentials stringCredentials) {
                //do nothing
            }

            @Override
            public void sendBuildStatus(String s, String s1, TuleapBuildStatus tuleapBuildStatus, TuleapAccessToken tuleapAccessToken) {
                //do nothing
            }

            @Override
            public GitCommit getCommit(String s, String s1, TuleapAccessToken tuleapAccessToken) {
                return null;
            }

            @Override
            public List<GitTreeContent> getTree(String s, String s1, String s2, TuleapAccessToken tuleapAccessToken) throws TreeNotFoundException {
                return null;
            }

            @Override
            public GitFileContent getFileContent(String s, String s1, String s2, TuleapAccessToken tuleapAccessToken) throws FileContentNotFoundException {
                return new GitFileContentStub("SSdtIGhlcmUsIEknbSBub3QgaGVyZQ==");
            }

            @Override
            public List<GitPullRequest> getPullRequests(String repositoryId, TuleapAccessToken token) {
                return null;
            }

            @Override
            public List<GitBranch> getBranches(String repositoryId, TuleapAccessToken token) {
                return null;
            }
        };

        TuleapSCMFile scmRootDirectory = new TuleapSCMFile(gitApi, "4", "master", new TuleapAccessTokenStub());
        TuleapSCMFile fileToDecode = (TuleapSCMFile) scmRootDirectory.newChild("whatever", false);

        String expectedDecodedContent = "I'm here, I'm not here";
        String decodedContent = new BufferedReader(new InputStreamReader(fileToDecode.content(), StandardCharsets.UTF_8)).readLine();

        assertEquals(expectedDecodedContent, decodedContent);
    }

    @Test(expected = IOException.class)
    public void itThrowsAExceptionBecauseWeCannotGetTheTextContentOfADirectory() throws IOException, InterruptedException {
        TuleapSCMFile scmRootDirectory = new TuleapSCMFile(null, "4", "master", new TuleapAccessTokenStub());
        scmRootDirectory.content();
    }

    @Test(expected = IOException.class)
    public void itThrowsAnExceptionWhenTheFileCannotBeRetrieved() throws IOException, InterruptedException {
        GitApi gitApi = new GitApi() {

            @Override
            public void sendBuildStatus(String s, String s1, TuleapBuildStatus tuleapBuildStatus, StringCredentials stringCredentials) {
                //do nothing
            }

            @Override
            public void sendBuildStatus(String s, String s1, TuleapBuildStatus tuleapBuildStatus, TuleapAccessToken tuleapAccessToken) {
                //do nothing
            }

            @Override
            public GitCommit getCommit(String s, String s1, TuleapAccessToken tuleapAccessToken) {
                return null;
            }

            @Override
            public List<GitTreeContent> getTree(String s, String s1, String s2, TuleapAccessToken tuleapAccessToken) throws TreeNotFoundException {
                return null;
            }

            @Override
            public GitFileContent getFileContent(String s, String s1, String s2, TuleapAccessToken tuleapAccessToken) throws FileContentNotFoundException {
                throw new FileContentNotFoundException();
            }

            @Override
            public List<GitPullRequest> getPullRequests(String repositoryId, TuleapAccessToken token) {
                return null;
            }

            @Override
            public List<GitBranch> getBranches(String repositoryId, TuleapAccessToken token) {
                return null;
            }
        };

        TuleapSCMFile scmRootDirectory = new TuleapSCMFile(gitApi, "4", "master", new TuleapAccessTokenStub());
        TuleapSCMFile fileToDecode = (TuleapSCMFile) scmRootDirectory.newChild("whatever", false);

        fileToDecode.content();
    }
}
