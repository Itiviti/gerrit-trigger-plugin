/*
 *  The MIT License
 *
 *  Copyright 2010 Sony Ericsson Mobile Communications. All rights reserved.
 *  Copyright 2012, 2013 Sony Mobile Communications AB. All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.sonyericsson.hudson.plugins.gerrit.trigger.spec.gerritnotifier;

import com.sonyericsson.hudson.plugins.gerrit.trigger.config.BuildStatus;
import com.sonyericsson.hudson.plugins.gerrit.trigger.config.IGerritHudsonTriggerConfig;
import com.sonyericsson.hudson.plugins.gerrit.trigger.extensions.GerritTriggeredBuildListener;
import com.sonyericsson.hudson.plugins.gerrit.trigger.gerritnotifier.GerritMessageProvider;
import com.sonyericsson.hudson.plugins.gerrit.trigger.gerritnotifier.GerritNotifier;
import com.sonyericsson.hudson.plugins.gerrit.trigger.gerritnotifier.model.BuildMemory;
import com.sonyericsson.hudson.plugins.gerrit.trigger.hudsontrigger.GerritTrigger;
import com.sonyericsson.hudson.plugins.gerrit.trigger.mock.Setup;
import com.sonymobile.tools.gerrit.gerritevents.GerritCmdRunner;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.PatchsetCreated;
import hudson.EnvVars;
import hudson.ExtensionList;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.io.IOException;

import static com.sonyericsson.hudson.plugins.gerrit.trigger.config.Constants.CODE_REVIEW_LABEL;
import static com.sonyericsson.hudson.plugins.gerrit.trigger.config.Constants.VERIFIED_LABEL;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Scenario tests.
 *
 * @author Robert Sandell &lt;robert.sandell@sonyericsson.com&gt;
 */
public class SpecGerritVerifiedSetterTest {

    private TaskListener taskListener;
    private GerritCmdRunner mockGerritCmdRunner;
    private AbstractBuild build;
    private EnvVars env;
    private AbstractProject project;
    private GerritTrigger trigger;
    private Jenkins jenkins;
    private MockedStatic<GerritMessageProvider> messageProviderMockedStatic;
    private MockedStatic<Jenkins> jenkinsMockedStatic;
    private MockedStatic<GerritTriggeredBuildListener> triggeredBuildListenerMockedStatic;

    /**
     * Prepare all the mocks.
     *
     * @throws Exception if so
     */
    @Before
    public void setUp() throws Exception {
        messageProviderMockedStatic = mockStatic(GerritMessageProvider.class);
        messageProviderMockedStatic.when(GerritMessageProvider::all).thenReturn(null);

        taskListener = mock(TaskListener.class);

        mockGerritCmdRunner = mock(GerritCmdRunner.class);

        build = mock(AbstractBuild.class);

        env = Setup.createEnvVars();
        when(build.getEnvironment(taskListener)).thenReturn(env);
        when(build.getId()).thenReturn("1");
        project = mock(AbstractProject.class);
        doReturn("MockProject").when(project).getFullName();
        when(build.getProject()).thenReturn(project);
        when(build.getParent()).thenReturn(project);
        doReturn(build).when(project).getBuild(anyString());

        trigger = mock(GerritTrigger.class);
        when(trigger.getLabelVote(CODE_REVIEW_LABEL, BuildStatus.SUCCESSFUL)).thenReturn(null);
        when(trigger.getLabelVote(VERIFIED_LABEL, BuildStatus.SUCCESSFUL)).thenReturn(null);
        when(trigger.getLabelVote(CODE_REVIEW_LABEL, BuildStatus.FAILED)).thenReturn(null);
        when(trigger.getLabelVote(VERIFIED_LABEL, BuildStatus.FAILED)).thenReturn(null);
        Setup.setTrigger(trigger, project);

        jenkinsMockedStatic = mockStatic(Jenkins.class);
        jenkins = mock(Jenkins.class);
        jenkinsMockedStatic.when(Jenkins::getInstanceOrNull).thenReturn(jenkins);
        when(jenkins.getItemByFullName(eq("MockProject"), same(AbstractProject.class))).thenReturn(project);
        when(jenkins.getItemByFullName(eq("MockProject"), same(Job.class))).thenReturn(project);
        when(jenkins.getRootUrl()).thenReturn("http://localhost/");

        triggeredBuildListenerMockedStatic = mockStatic(GerritTriggeredBuildListener.class);
        triggeredBuildListenerMockedStatic.when(GerritTriggeredBuildListener::all).thenReturn(mock(ExtensionList.class));
    }

    @After
    public void tearDown() throws Exception {
        messageProviderMockedStatic.close();
        jenkinsMockedStatic.close();
        triggeredBuildListenerMockedStatic.close();

    }

    /**
     * A test.
     *
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     */
    @Test
    public void shouldCallGerritWithVerifiedOkFlagWhenBuildWasSuccessful()
            throws IOException, InterruptedException {

        when(build.getResult()).thenReturn(Result.SUCCESS);

        PatchsetCreated event = Setup.createPatchsetCreated();

        BuildMemory memory = new BuildMemory();
        memory.completed(event, build);

        IGerritHudsonTriggerConfig config = Setup.createMockableConfig();

        String parameterString = "gerrit review MSG=OK VERIFIED=<VERIFIED> CODEREVIEW=<CODE_REVIEW>";
        when(config.getGerritCmdBuildSuccessful()).thenReturn(parameterString);
        when(config.getLabelVote(VERIFIED_LABEL, BuildStatus.SUCCESSFUL)).thenReturn(1);
        when(config.getLabelVote(CODE_REVIEW_LABEL, BuildStatus.SUCCESSFUL)).thenReturn(1);

        GerritNotifier notifier = new GerritNotifier(config, mockGerritCmdRunner, jenkins);
        notifier.buildCompleted(memory.getMemoryImprint(event), taskListener);
        String parameterStringExpected = "gerrit review MSG=OK VERIFIED=1 CODEREVIEW=1";

        verify(mockGerritCmdRunner).sendCommand(parameterStringExpected);
    }

    /**
     * A test.
     *
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     */
    @Test
    public void shouldCallGerritWithVerifiedRejectFlagWhenBuildWasNotSuccessful()
            throws IOException, InterruptedException {

        when(build.getResult()).thenReturn(Result.FAILURE);
        PatchsetCreated event = Setup.createPatchsetCreated();
        BuildMemory memory = new BuildMemory();

        memory.completed(event, build);

        IGerritHudsonTriggerConfig config = Setup.createMockableConfig();

        String parameterString = "gerrit review MSG=Failed VERIFIED=<VERIFIED> CODEREVIEW=<CODE_REVIEW>";
        when(config.getGerritCmdBuildFailed()).thenReturn(parameterString);
        when(config.getLabelVote(VERIFIED_LABEL, BuildStatus.FAILED)).thenReturn(-1);
        when(config.getLabelVote(CODE_REVIEW_LABEL, BuildStatus.FAILED)).thenReturn(-1);

        GerritNotifier notifier = new GerritNotifier(config, mockGerritCmdRunner, jenkins);
        notifier.buildCompleted(memory.getMemoryImprint(event), taskListener);
        String parameterStringExpected = "gerrit review MSG=Failed VERIFIED=-1 CODEREVIEW=-1";

        verify(mockGerritCmdRunner).sendCommand(parameterStringExpected);
    }

    /**
     * A test.
     *
     * @throws IOException          IOException
     * @throws InterruptedException InterruptedException
     */
    @Test
    public void shouldCallGerritWithVerifiedFailedFlagWhenBuildOneBuildFailedAndAnotherSuccessful()
            throws IOException, InterruptedException {

        when(build.getResult()).thenReturn(Result.SUCCESS);

        PatchsetCreated event = Setup.createPatchsetCreated();

        BuildMemory memory = new BuildMemory();
        memory.completed(event, build);

        build = mock(AbstractBuild.class);
        when(build.getResult()).thenReturn(Result.FAILURE);
        env = Setup.createEnvVars();
        when(build.getEnvironment(taskListener)).thenReturn(env);
        when(build.getId()).thenReturn("1");
        project = mock(AbstractProject.class);
        doReturn("MockProject2").when(project).getFullName();
        doReturn(build).when(project).getBuild(anyString());
        when(build.getProject()).thenReturn(project);
        when(build.getParent()).thenReturn(project);
        when(jenkins.getItemByFullName(eq("MockProject2"), same(AbstractProject.class))).thenReturn(project);
        when(jenkins.getItemByFullName(eq("MockProject2"), same(Job.class))).thenReturn(project);

        trigger = mock(GerritTrigger.class);
        when(trigger.getLabelVote(CODE_REVIEW_LABEL, BuildStatus.FAILED)).thenReturn(null);
        when(trigger.getLabelVote(VERIFIED_LABEL, BuildStatus.FAILED)).thenReturn(null);
        Setup.setTrigger(trigger, project);

        memory.completed(event, build);

        IGerritHudsonTriggerConfig config = Setup.createMockableConfig();

        String parameterString = "gerrit review MSG=FAILED VERIFIED=<VERIFIED> CODEREVIEW=<CODE_REVIEW>";
        when(config.getGerritCmdBuildFailed()).thenReturn(parameterString);
        when(config.getLabelVote(VERIFIED_LABEL, BuildStatus.SUCCESSFUL)).thenReturn(1);
        when(config.getLabelVote(CODE_REVIEW_LABEL, BuildStatus.SUCCESSFUL)).thenReturn(1);
        when(config.getLabelVote(CODE_REVIEW_LABEL, BuildStatus.FAILED)).thenReturn(-1);
        when(config.getLabelVote(VERIFIED_LABEL, BuildStatus.FAILED)).thenReturn(-1);

        GerritNotifier notifier = new GerritNotifier(config, mockGerritCmdRunner, jenkins);
        notifier.buildCompleted(memory.getMemoryImprint(event), taskListener);
        String parameterStringExpected = "gerrit review MSG=FAILED VERIFIED=-1 CODEREVIEW=-1";

        verify(mockGerritCmdRunner).sendCommand(parameterStringExpected);
    }
}
