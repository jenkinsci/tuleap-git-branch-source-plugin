package org.jenkinsci.plugins.tuleap_git_branch_source;

import hudson.plugins.git.GitChangeSet;
import hudson.scm.EditType;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TuleapBrowserTest {

    @Test
    public void testItReturnsNoDiffLinkIfTheFileIsNotEditedInTheLastCommit() throws IOException {
        String parentLine = "parent h4sh_s0m3";
        List<String> lines = Collections.singletonList(parentLine);
        GitChangeSet changeSet = new GitChangeSet(lines, false);

        GitChangeSet.Path path = mock(GitChangeSet.Path.class);
        when(path.getEditType()).thenReturn(EditType.ADD);
        when(path.getSrc()).thenReturn("source/file");
        when(path.getDst()).thenReturn("dest/file");
        when(path.getChangeSet()).thenReturn(changeSet);

        TuleapBrowser browser = new TuleapBrowser("https://tuleap.example.com/plugins/git/naha/repo01");

        assertNull(browser.getDiffLink(path));
    }

    @Test
    public void testItReturnsNoDiffLinkIfTheFileDoesNotHaveSourcePath() throws IOException {
        String parentLine = "parent h4sh_s0m3";
        List<String> lines = Collections.singletonList(parentLine);
        GitChangeSet changeSet = new GitChangeSet(lines, false);

        GitChangeSet.Path path = mock(GitChangeSet.Path.class);
        when(path.getEditType()).thenReturn(EditType.EDIT);
        when(path.getSrc()).thenReturn(null);
        when(path.getDst()).thenReturn("dest/file");
        when(path.getChangeSet()).thenReturn(changeSet);

        TuleapBrowser browser = new TuleapBrowser("https://tuleap.example.com/plugins/git/naha/repo01");

        assertNull(browser.getDiffLink(path));
    }

    @Test
    public void testItReturnsNoDiffLinkIfTheFileDoesNotHaveDestinationPath() throws IOException {
        String parentLine = "parent h4sh_s0m3";
        List<String> lines = Collections.singletonList(parentLine);
        GitChangeSet changeSet = new GitChangeSet(lines, false);

        GitChangeSet.Path path = mock(GitChangeSet.Path.class);
        when(path.getEditType()).thenReturn(EditType.EDIT);
        when(path.getSrc()).thenReturn("source/file");
        when(path.getDst()).thenReturn(null);
        when(path.getChangeSet()).thenReturn(changeSet);

        TuleapBrowser browser = new TuleapBrowser("https://tuleap.example.com/plugins/git/naha/repo01");

        assertNull(browser.getDiffLink(path));
    }

    @Test
    public void testItReturnsNoDiffLinkIfTheFileDoesNotHaveParentCommit() throws IOException {
        String treeLine = "tree h4sh_s0m3";
        List<String> lines = Collections.singletonList(treeLine);
        GitChangeSet changeSet = new GitChangeSet(lines, false);

        GitChangeSet.Path path = mock(GitChangeSet.Path.class);
        when(path.getEditType()).thenReturn(EditType.EDIT);
        when(path.getSrc()).thenReturn("source/file");
        when(path.getDst()).thenReturn("dest/file");
        when(path.getChangeSet()).thenReturn(changeSet);

        TuleapBrowser browser = new TuleapBrowser("https://tuleap.example.com/plugins/git/naha/repo01");

        assertNull(browser.getDiffLink(path));
    }

    @Test
    public void testItReturnsTheDiffLink() throws IOException {
        String treeLine = "parent h4sh_s0m3";
        List<String> lines = Collections.singletonList(treeLine);
        GitChangeSet changeSet = new GitChangeSet(lines, false);

        GitChangeSet.Path path = mock(GitChangeSet.Path.class);
        when(path.getEditType()).thenReturn(EditType.EDIT);
        when(path.getSrc()).thenReturn("source/file");
        when(path.getDst()).thenReturn("dest/file");
        when(path.getChangeSet()).thenReturn(changeSet);

        TuleapBrowser browser = new TuleapBrowser("https://tuleap.example.com/plugins/git/naha/repo01");

        assertEquals(new URL("https://tuleap.example.com/plugins/git/naha/repo01?a=commitdiff&h=null"), browser.getDiffLink(path));
    }

    @Test
    public void testItDoesNotReturnTheFileLinkContentIfTheFileIsInDeleteMode() throws IOException {
        GitChangeSet.Path path = mock(GitChangeSet.Path.class);
        when(path.getEditType()).thenReturn(EditType.DELETE);

        TuleapBrowser browser = new TuleapBrowser("https://tuleap.example.com/plugins/git/naha/repo01");

        assertNull(browser.getFileLink(path));
    }

    @Test
    public void testItReturnsTheFileLinkContentIfTheFileIsNotInDeleteMode() throws IOException {
        String commitIdLine = "commit h4sh_s0m3";
        List<String> lines = Collections.singletonList(commitIdLine);
        GitChangeSet changeSet = new GitChangeSet(lines, false);

        GitChangeSet.Path path = mock(GitChangeSet.Path.class);
        when(path.getEditType()).thenReturn(EditType.EDIT).thenReturn(EditType.ADD);
        when(path.getChangeSet()).thenReturn(changeSet);

        TuleapBrowser browser = new TuleapBrowser("https://tuleap.example.com/plugins/git/naha/repo01");

        assertEquals(new URL("https://tuleap.example.com/plugins/git/naha/repo01?a=blob&hb=h4sh_s0m3&f=null"), browser.getFileLink(path));
        assertEquals(new URL("https://tuleap.example.com/plugins/git/naha/repo01?a=blob&hb=h4sh_s0m3&f=null"), browser.getFileLink(path));
    }
}
