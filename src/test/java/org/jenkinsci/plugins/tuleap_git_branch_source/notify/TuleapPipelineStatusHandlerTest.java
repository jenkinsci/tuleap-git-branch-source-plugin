package org.jenkinsci.plugins.tuleap_git_branch_source.notify;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogParser;
import hudson.scm.SCM;
import io.jenkins.plugins.tuleap_api.client.GitRepository;
import io.jenkins.plugins.tuleap_api.client.Project;
import io.jenkins.plugins.tuleap_api.client.internals.entities.TuleapBuildStatus;
import jenkins.scm.api.*;
import jenkins.scm.api.trait.SCMSourceTrait;
import org.jenkinsci.plugins.tuleap_git_branch_source.TuleapSCMSource;
import org.jenkinsci.plugins.tuleap_git_branch_source.trait.TuleapCommitNotificationTrait;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

public class TuleapPipelineStatusHandlerTest {

    private TuleapPipelineStatusNotifier notifier;
    private MockedStatic<SCMSource.SourceByItem> sourceByItem;

    private TuleapPipelineStatusHandler handler;

    @Before
    public void setUp() {
        this.notifier = Mockito.mock(TuleapPipelineStatusNotifier.class);
        this.sourceByItem = Mockito.mockStatic(SCMSource.SourceByItem.class);

        this.handler = new TuleapPipelineStatusHandler(this.notifier);
    }

    @After
    public void tearDown() {
        this.sourceByItem.close();
    }

    @Test
    public void testItDoesNothingWhenTheSCMSourceIsNotATuleapSCMSource() {
        final FreeStyleBuild build = mock(FreeStyleBuild.class);
        final SCMSource source = new SCMSource() {
            @Override
            protected void retrieve(SCMSourceCriteria criteria, @NotNull SCMHeadObserver observer, SCMHeadEvent<?> event, @NotNull TaskListener listener) throws IOException, InterruptedException {
            }

            @NotNull
            @Override
            public SCM build(@NotNull SCMHead head, SCMRevision revision) {
                return new SCM() {
                    @Override
                    public ChangeLogParser createChangeLogParser() {
                        return null;
                    }
                };
            }
        };

        List<SCMSourceTrait> traits = Collections.singletonList(new TuleapCommitNotificationTrait());
        source.setTraits(traits);

        final PrintStream logger = mock(PrintStream.class);
        final FreeStyleProject freestyleProject = mock(FreeStyleProject.class);

        when(build.getParent()).thenReturn(freestyleProject);

        this.sourceByItem.when(() -> SCMSource.SourceByItem.findSource(freestyleProject)).thenReturn(source);

        this.handler.handleCommitNotification(
            build,
            logger,
            TuleapBuildStatus.success
        );

        verify(this.notifier, never()).sendBuildStatusToTuleap(
            eq(build),
            eq(logger),
            eq(TuleapBuildStatus.success),
            any()
        );
    }

    @Test
    public void testItDoesNothingWhenTheTraitsIsNotSet() {
        final FreeStyleBuild build = mock(FreeStyleBuild.class);
        final TuleapSCMSource source = this.getTuleapSCMSource();

        List<SCMSourceTrait> traits = Collections.emptyList();
        source.setTraits(traits);

        final PrintStream logger = mock(PrintStream.class);
        final FreeStyleProject freestyleProject = mock(FreeStyleProject.class);

        when(build.getParent()).thenReturn(freestyleProject);

        this.sourceByItem.when(() -> SCMSource.SourceByItem.findSource(freestyleProject)).thenReturn(source);

        this.handler.handleCommitNotification(
            build,
            logger,
            TuleapBuildStatus.success
        );

        verify(this.notifier, never()).sendBuildStatusToTuleap(
            build,
            logger,
            TuleapBuildStatus.success,
            source
        );
    }

    @Test
    public void testItDoesNothingWhenTheCommitNotificationTraitsIsNotSet() {
        final FreeStyleBuild build = mock(FreeStyleBuild.class);
        final TuleapSCMSource source = this.getTuleapSCMSource();

        List<SCMSourceTrait> traits = Collections.singletonList(new TuleapCommitNotificationTrait());
        source.setTraits(traits);

        final PrintStream logger = mock(PrintStream.class);
        final FreeStyleProject freestyleProject = mock(FreeStyleProject.class);

        when(build.getParent()).thenReturn(freestyleProject);

        this.sourceByItem.when(() -> SCMSource.SourceByItem.findSource(freestyleProject)).thenReturn(source);

        this.handler.handleCommitNotification(
            build,
            logger,
            TuleapBuildStatus.success
        );

        verify(this.notifier, atMostOnce()).sendBuildStatusToTuleap(
            build,
            logger,
            TuleapBuildStatus.success,
            source
        );
    }

    private TuleapSCMSource getTuleapSCMSource() {
        return new TuleapSCMSource(
            new Project() {
                @Override
                public Integer getId() {
                    return null;
                }

                @Override
                public String getShortname() {
                    return null;
                }

                @Override
                public String getLabel() {
                    return null;
                }

                @Override
                public String getUri() {
                    return null;
                }
            },
            new GitRepository() {
                @Override
                public String getName() {
                    return null;
                }

                @Override
                public Integer getId() {
                    return null;
                }

                @Override
                public String getPath() {
                    return null;
                }
            }
        );
    }
}
