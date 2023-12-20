/*
 *  The MIT License
 *
 *  Copyright 2010 Sony Mobile Communications Inc. All rights reserved.
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
package com.sonyericsson.hudson.plugins.gerrit.trigger.config;

import com.sonyericsson.hudson.plugins.gerrit.trigger.VerdictCategory;
import com.sonyericsson.hudson.plugins.gerrit.trigger.hudsontrigger.data.BuildCancellationPolicy;
import com.sonymobile.tools.gerrit.gerritevents.GerritDefaultValues;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Provider;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.ChangeBasedEvent;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.GerritTriggeredEvent;
import com.sonymobile.tools.gerrit.gerritevents.dto.rest.Notify;
import com.sonymobile.tools.gerrit.gerritevents.ssh.Authentication;
import com.sonymobile.tools.gerrit.gerritevents.watchdog.WatchTimeExceptionData;
import com.sonymobile.tools.gerrit.gerritevents.watchdog.WatchTimeExceptionData.Time;
import com.sonymobile.tools.gerrit.gerritevents.watchdog.WatchTimeExceptionData.TimeSpan;
import hudson.util.Secret;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

//CS IGNORE LineLength FOR NEXT 11 LINES. REASON: static import.
import static com.sonyericsson.hudson.plugins.gerrit.trigger.config.Constants.CODE_REVIEW_LABEL;
import static com.sonyericsson.hudson.plugins.gerrit.trigger.config.Constants.VERIFIED_LABEL;
import static com.sonymobile.tools.gerrit.gerritevents.GerritDefaultValues.DEFAULT_BUILD_SCHEDULE_DELAY;
import static com.sonymobile.tools.gerrit.gerritevents.GerritDefaultValues.DEFAULT_GERRIT_AUTH_KEY_FILE;
import static com.sonymobile.tools.gerrit.gerritevents.GerritDefaultValues.DEFAULT_GERRIT_AUTH_KEY_FILE_PASSWORD;
import static com.sonymobile.tools.gerrit.gerritevents.GerritDefaultValues.DEFAULT_GERRIT_HOSTNAME;
import static com.sonymobile.tools.gerrit.gerritevents.GerritDefaultValues.DEFAULT_GERRIT_SSH_PORT;
import static com.sonymobile.tools.gerrit.gerritevents.GerritDefaultValues.DEFAULT_GERRIT_PROXY;
import static com.sonymobile.tools.gerrit.gerritevents.GerritDefaultValues.DEFAULT_GERRIT_USERNAME;
import static com.sonymobile.tools.gerrit.gerritevents.GerritDefaultValues.DEFAULT_NR_OF_RECEIVING_WORKER_THREADS;
import static com.sonymobile.tools.gerrit.gerritevents.GerritDefaultValues.DEFAULT_NR_OF_SENDING_WORKER_THREADS;

/**
 * Configuration bean for the global configuration.
 *
 * @author Robert Sandell &lt;robert.sandell@sonyericsson.com&gt;
 */
public class Config implements IGerritHudsonTriggerConfig {

    /**
     * Default verified vote to Gerrit when a build is started.
     */
    public static final int DEFAULT_GERRIT_BUILD_STARTED_VERIFIED_VALUE = 0;
    /**
     * Default verified vote to Gerrit when a build is unstable.
     */
    public static final int DEFAULT_GERRIT_BUILD_UNSTABLE_VERIFIED_VALUE = 0;
    /**
     * Default verified vote to Gerrit when a build is failed.
     */
    public static final int DEFAULT_GERRIT_BUILD_FAILURE_VERIFIED_VALUE = -1;
    /**
     * Default verified vote to Gerrit when a build is successful.
     */
    public static final int DEFAULT_GERRIT_BUILD_SUCCESSFUL_VERIFIED_VALUE = 1;
    /**
     * Default verified vote to Gerrit when a build is not built.
     */
    public static final int DEFAULT_GERRIT_BUILD_NOT_BUILT_VERIFIED_VALUE = 0;
    /**
     * Default verified vote to Gerrit when a build is aborted.
     */
    public static final int DEFAULT_GERRIT_BUILD_ABORTED_VERIFIED_VALUE = 0;
   /**
-     * Default code review vote to Gerrit when a build is started.
     */
    public static final int DEFAULT_GERRIT_BUILD_STARTED_CODE_REVIEW_VALUE = 0;
   /**
     * Default code review vote to Gerrit when a build is unstable.
     */
    public static final int DEFAULT_GERRIT_BUILD_UNSTABLE_CODE_REVIEW_VALUE = -1;
   /**
     * Default code review vote to Gerrit when a build is failed.
     */
    public static final int DEFAULT_GERRIT_BUILD_FAILURE_CODE_REVIEW_VALUE = 0;
    /**
     * Default code review vote to Gerrit when a build is successful.
     */
    public static final int DEFAULT_GERRIT_BUILD_SUCCESSFUL_CODE_REVIEW_VALUE = 0;
    /**
     * Default code review vote to Gerrit when a build is not built.
     */
    public static final int DEFAULT_GERRIT_BUILD_NOT_BUILT_CODE_REVIEW_VALUE = 0;
    /**
     * Default code review vote to Gerrit when a build is aborted.
     */
    public static final int DEFAULT_GERRIT_BUILD_ABORTED_CODE_REVIEW_VALUE = 0;
    /**
     * Default value indicating if the Gerrit server should be used to fetch project names.
     */
    public static final boolean DEFAULT_ENABLE_PROJECT_AUTO_COMPLETION = true;
    /**
     * Default value for the dynamic config refresh interval.
     */
    public static final int DEFAULT_DYNAMIC_CONFIG_REFRESH_INTERVAL = 30;

    /**
     * Default value showing how many seconds between startup and initial project list fetch.
     */
    public static final int DEFAULT_PROJECT_LIST_FETCH_DELAY = 0;
    /**
     * Default value showing how many seconds between project list fetches.
     * <p>Only used for Gerrit servers with version &lt; 2.12</p>
     */
    public static final int DEFAULT_PROJECT_LIST_REFRESH_INTERVAL = 3600;
    /**
     * Default timeout value in minutes for the connection watchdog.
     */
    public static final int DEFAULT_GERRIT_WATCHDOG_TIMEOUT_MINUTES = 0;
    /**
     * Default manual trigger enabled.
     */
    public static final boolean DEFAULT_ENABLE_MANUAL_TRIGGER = true;
    /**
     * Default plug-in messages enabled.
     */
    public static final boolean DEFAULT_ENABLE_PLUGIN_MESSAGES = true;
    /**
     * Default topic association enabled.
     */
    public static final boolean DEFAULT_ENABLE_TOPIC_ASSOCIATION = false;
    /**
     * Default value for the option to allow triggering on messages for any user.
     */
    public static final boolean DEFAULT_TRIGGER_ON_ALL_COMMENTS = false;
    /**
     * Default value for {@link #isGerritBuildCurrentPatchesOnly()}.
     */
    public static final boolean DEFAULT_BUILD_CURRENT_PATCHES_ONLY = false;
    /**
     * Global default for notification level.
     */
    public static final Notify DEFAULT_NOTIFICATION_LEVEL = Notify.ALL;

    private String gerritHostName;
    private int gerritSshPort;
    private String gerritProxy;
    private String gerritUserName;
    private String gerritEMail;
    private File gerritAuthKeyFile;
    private Secret gerritAuthKeyFilePassword;
    private boolean useRestApi;
    private String gerritHttpUserName;
    private Secret gerritHttpPassword;
    private boolean restCodeReview;
    private boolean restVerified;
    @Deprecated
    private transient boolean gerritBuildCurrentPatchesOnly;
    @Deprecated
    private transient int numberOfWorkerThreads;
    private String gerritFrontEndUrl;
    private boolean enableManualTrigger;
    private boolean enablePluginMessages;
    private boolean triggerOnAllComments;
    @Deprecated
    private transient int numberOfSendingWorkerThreads;
    private int buildScheduleDelay;
    private int dynamicConfigRefreshInterval;
    private boolean enableProjectAutoCompletion;
    private int projectListRefreshInterval;
    @Deprecated
    private transient boolean loadProjectListOnStartup;
    private int projectListFetchDelay;
    private List<VerdictCategory> categories;
    private Map<BuildStatus, CommandBuilder> buildStatusCommandBuilderMap = new HashMap<>();
    private ReplicationConfig replicationConfig;
    private int watchdogTimeoutMinutes;
    private WatchTimeExceptionData watchTimeExceptionData;
    private Notify notificationLevel;
    private BuildCancellationPolicy buildCurrentPatchesOnly;
    private boolean voteSameTopic;

    /**
     * Constructor.
     *
     * @param formData the data.
     */
    public Config(JSONObject formData) {
        setValues(formData);
    }

    /**
     * Copy constructor.
     *
     * @param config the Config object to be copied.
     */
    public Config(IGerritHudsonTriggerConfig config) {
        gerritHostName = config.getGerritHostName();
        gerritSshPort = config.getGerritSshPort();
        gerritProxy = config.getGerritProxy();
        gerritUserName = config.getGerritUserName();
        gerritEMail = config.getGerritEMail();
        notificationLevel = config.getNotificationLevel();
        gerritAuthKeyFile = new File(config.getGerritAuthKeyFile().getPath());
        gerritAuthKeyFilePassword = Secret.fromString(config.getGerritAuthKeyFilePassword());
        useRestApi = config.isUseRestApi();
        gerritHttpUserName = config.getGerritHttpUserName();
        gerritHttpPassword = Secret.fromString(config.getGerritHttpPassword());
        restCodeReview = config.isRestCodeReview();
        restVerified = config.isRestVerified();
        gerritBuildCurrentPatchesOnly = config.isGerritBuildCurrentPatchesOnly();
        numberOfWorkerThreads = config.getNumberOfReceivingWorkerThreads();
        numberOfSendingWorkerThreads = config.getNumberOfSendingWorkerThreads();
        gerritFrontEndUrl = config.getGerritFrontEndUrl();
        enableManualTrigger = config.isEnableManualTrigger();
        enablePluginMessages = config.isEnablePluginMessages();
        triggerOnAllComments = config.isTriggerOnAllComments();
        buildScheduleDelay = config.getBuildScheduleDelay();
        dynamicConfigRefreshInterval = config.getDynamicConfigRefreshInterval();
        enableProjectAutoCompletion = config.isEnableProjectAutoCompletion();
        projectListFetchDelay = config.getProjectListFetchDelay();
        projectListRefreshInterval = config.getProjectListRefreshInterval();

        if (config.getCategories() != null) {
            categories = new LinkedList<>();
            for (VerdictCategory cat : config.getCategories()) {
                categories.add(cat.clone());
            }
        }

        buildStatusCommandBuilderMap.put(BuildStatus.STARTED, CommandBuilder.fromString(config.getGerritCmdBuildStarted()));
        buildStatusCommandBuilderMap.put(BuildStatus.SUCCESSFUL, CommandBuilder.fromString(config.getGerritCmdBuildSuccessful()));
        buildStatusCommandBuilderMap.put(BuildStatus.FAILED, CommandBuilder.fromString(config.getGerritCmdBuildFailed()));
        buildStatusCommandBuilderMap.put(BuildStatus.UNSTABLE, CommandBuilder.fromString(config.getGerritCmdBuildUnstable()));
        buildStatusCommandBuilderMap.put(BuildStatus.NOT_BUILT, CommandBuilder.fromString(config.getGerritCmdBuildNotBuilt()));
        buildStatusCommandBuilderMap.put(BuildStatus.ABORTED, CommandBuilder.fromString(config.getGerritCmdBuildAborted()));

        if (config.getReplicationConfig() != null) {
            replicationConfig = new ReplicationConfig(config.getReplicationConfig());
        }
        watchdogTimeoutMinutes = config.getWatchdogTimeoutMinutes();
        watchTimeExceptionData = addWatchTimeExceptionData(config.getExceptionData());
    }

    @Override
    public void setValues(JSONObject formData) {
        gerritHostName = formData.optString("gerritHostName", DEFAULT_GERRIT_HOSTNAME);
        gerritSshPort = formData.optInt("gerritSshPort", DEFAULT_GERRIT_SSH_PORT);
        gerritProxy = formData.optString("gerritProxy", DEFAULT_GERRIT_PROXY);
        gerritUserName = formData.optString("gerritUserName", DEFAULT_GERRIT_USERNAME);
        gerritEMail = formData.optString("gerritEMail", "");
        notificationLevel = Notify.valueOf(formData.optString("notificationLevel",
                Config.DEFAULT_NOTIFICATION_LEVEL.toString()));
        String file = formData.optString("gerritAuthKeyFile", null);
        if (file != null) {
            gerritAuthKeyFile = new File(file);
        } else {
            gerritAuthKeyFile = DEFAULT_GERRIT_AUTH_KEY_FILE;
        }
        gerritAuthKeyFilePassword = Secret.fromString(formData.optString(
                "gerritAuthKeyFilePassword",
                DEFAULT_GERRIT_AUTH_KEY_FILE_PASSWORD));

        if (formData.has("buildCurrentPatchesOnly")) {
            JSONObject currentPatchesOnly = formData.getJSONObject("buildCurrentPatchesOnly");
            buildCurrentPatchesOnly = BuildCancellationPolicy.createPolicyFromJSON(currentPatchesOnly);
        } else {
            buildCurrentPatchesOnly = new BuildCancellationPolicy();
        }

        voteSameTopic = formData.optBoolean("voteSameTopic", false);

        numberOfWorkerThreads = formData.optInt(
                "numberOfReceivingWorkerThreads",
                DEFAULT_NR_OF_RECEIVING_WORKER_THREADS);
        if (numberOfWorkerThreads <= 0) {
            numberOfWorkerThreads = DEFAULT_NR_OF_RECEIVING_WORKER_THREADS;
        }
        numberOfSendingWorkerThreads = formData.optInt(
                "numberOfSendingWorkerThreads",
                DEFAULT_NR_OF_SENDING_WORKER_THREADS);
        if (numberOfSendingWorkerThreads <= 0) {
            numberOfSendingWorkerThreads = DEFAULT_NR_OF_SENDING_WORKER_THREADS;
        }

        buildStatusCommandBuilderMap.put(BuildStatus.STARTED, CommandBuilder.fromString(formData.optString("gerritVerifiedCmdBuildStarted", null),
                new CommandBuilder().WithMessage("Build Started <BUILDURL> <STARTED_STATS>")));
        buildStatusCommandBuilderMap.put(BuildStatus.SUCCESSFUL, CommandBuilder.fromString(formData.optString("gerritVerifiedCmdBuildSuccessful", null),
                new CommandBuilder().WithMessage("Build Successful <BUILDS_STATS>")));
        buildStatusCommandBuilderMap.put(BuildStatus.FAILED, CommandBuilder.fromString(formData.optString("gerritVerifiedCmdBuildFailed", null),
                new CommandBuilder().WithMessage("Build Failed <BUILDS_STATS>")));
        buildStatusCommandBuilderMap.put(BuildStatus.UNSTABLE, CommandBuilder.fromString(formData.optString("gerritVerifiedCmdBuildUnstable", null),
                new CommandBuilder().WithMessage("Build Unstable <BUILDS_STATS>")));
        buildStatusCommandBuilderMap.put(BuildStatus.NOT_BUILT, CommandBuilder.fromString(formData.optString("gerritVerifiedCmdBuildNotBuilt", null),
                new CommandBuilder().WithMessage("No Builds Executed <BUILDS_STATS>")));
        buildStatusCommandBuilderMap.put(BuildStatus.ABORTED, CommandBuilder.fromString(formData.optString("gerritVerifiedCmdBuildAborted", null),
                new CommandBuilder().WithMessage("Build Aborted <BUILDS_STATS>")));

        gerritFrontEndUrl = formData.optString(
                "gerritFrontEndUrl",
                DEFAULT_GERRIT_HOSTNAME);
        enableManualTrigger = formData.optBoolean(
                "enableManualTrigger",
                DEFAULT_ENABLE_MANUAL_TRIGGER);
        enablePluginMessages = formData.optBoolean(
                "enablePluginMessages",
                DEFAULT_ENABLE_PLUGIN_MESSAGES);
        triggerOnAllComments = formData.optBoolean(
                "triggerOnAllComments",
                DEFAULT_TRIGGER_ON_ALL_COMMENTS);
        buildScheduleDelay = formData.optInt(
                "buildScheduleDelay",
                DEFAULT_BUILD_SCHEDULE_DELAY);
        if (buildScheduleDelay < 0) {
            buildScheduleDelay = 0;
        }
        dynamicConfigRefreshInterval = formData.optInt(
                "dynamicConfigRefreshInterval",
                DEFAULT_DYNAMIC_CONFIG_REFRESH_INTERVAL);

        projectListFetchDelay = formData.optInt(
                "projectListFetchDelay",
                DEFAULT_PROJECT_LIST_FETCH_DELAY);

        projectListRefreshInterval = formData.optInt(
                "projectListRefreshInterval",
                DEFAULT_PROJECT_LIST_REFRESH_INTERVAL);
        enableProjectAutoCompletion = formData.optBoolean(
                "enableProjectAutoCompletion",
                DEFAULT_ENABLE_PROJECT_AUTO_COMPLETION);

        addCategories(formData);

        watchdogTimeoutMinutes = formData.optInt("watchdogTimeoutMinutes", DEFAULT_GERRIT_WATCHDOG_TIMEOUT_MINUTES);
        watchTimeExceptionData = addWatchTimeExceptionData(formData);

        if (formData.has("useRestApi")) {
            useRestApi = true;
            JSONObject restApi = formData.getJSONObject("useRestApi");
            gerritHttpUserName = restApi.optString("gerritHttpUserName", "");
            gerritHttpPassword = Secret.fromString(restApi.optString("gerritHttpPassword", ""));
            restCodeReview = restApi.optBoolean("restCodeReview", true);
            restVerified = restApi.optBoolean("restVerified", true);
        } else {
            useRestApi = false;
        }

        replicationConfig = ReplicationConfig.createReplicationConfigFromJSON(formData);
    }

    /**
     * Adds {@link VerdictCategory}s to the categories based on the formData.
     * @param formData the formData as a JSONObject.
     */
    private void addCategories(JSONObject formData) {
        categories = new LinkedList<>();

        if (formData.isEmpty())
        {
            categories.add(new VerdictCategory(CODE_REVIEW_LABEL,
                    CODE_REVIEW_LABEL,
                    DEFAULT_GERRIT_BUILD_STARTED_CODE_REVIEW_VALUE,
                    DEFAULT_GERRIT_BUILD_SUCCESSFUL_CODE_REVIEW_VALUE,
                    DEFAULT_GERRIT_BUILD_FAILURE_CODE_REVIEW_VALUE,
                    DEFAULT_GERRIT_BUILD_UNSTABLE_CODE_REVIEW_VALUE,
                    DEFAULT_GERRIT_BUILD_NOT_BUILT_CODE_REVIEW_VALUE,
                    DEFAULT_GERRIT_BUILD_ABORTED_CODE_REVIEW_VALUE));
            categories.add(new VerdictCategory(VERIFIED_LABEL, VERIFIED_LABEL,
                    DEFAULT_GERRIT_BUILD_STARTED_VERIFIED_VALUE,
                    DEFAULT_GERRIT_BUILD_SUCCESSFUL_VERIFIED_VALUE,
                    DEFAULT_GERRIT_BUILD_FAILURE_VERIFIED_VALUE,
                    DEFAULT_GERRIT_BUILD_UNSTABLE_VERIFIED_VALUE,
                    DEFAULT_GERRIT_BUILD_NOT_BUILT_VERIFIED_VALUE,
                    DEFAULT_GERRIT_BUILD_ABORTED_VERIFIED_VALUE));
        }

        if (formData.has("verdictCategories")) {
            Object cat = formData.get("verdictCategories");
            if (cat instanceof JSONArray) {
                for (Object jsonObject : (JSONArray)cat) {
                    VerdictCategory verdictCategory = VerdictCategory.fromJSON((JSONObject) jsonObject);

                    categories.add(verdictCategory);
                    updateBuildStatusCommands(verdictCategory);

                }
            } else if (cat instanceof JSONObject) {
                VerdictCategory verdictCategory = VerdictCategory.fromJSON((JSONObject) cat);

                categories.add(verdictCategory);
                updateBuildStatusCommands(verdictCategory);
            }
        }
    }

    /**
     * Adds a label to the existing build status commands.
     * @param verdictCategory the label to be added.
     */
    private void updateBuildStatusCommands(VerdictCategory verdictCategory) {
        for (Map.Entry<BuildStatus, CommandBuilder> entry : buildStatusCommandBuilderMap.entrySet()) {
            CommandBuilder commandBuilder = entry.getValue();
            commandBuilder.WithLabel(verdictCategory);
        }
    }

    /**
     * Adds the WatchTimeExceptionData from the form.
     *
     * @param formData the form.
     * @return the WatchTimeExceptionData
     */
    private WatchTimeExceptionData addWatchTimeExceptionData(JSONObject formData) {
        List<Integer> days = new LinkedList<Integer>();
        List<TimeSpan> exceptionTimes = new LinkedList<TimeSpan>();
        int[] daysAsInt = new int[]{};
        if (formData.has("watchdogExceptions")) {
            JSONObject jsonObject = formData.getJSONObject(("watchdogExceptions"));
            if (jsonObject.getBoolean(String.valueOf(Calendar.MONDAY))) {
                days.add(Calendar.MONDAY);
            }
            if (jsonObject.getBoolean(String.valueOf(Calendar.TUESDAY))) {
                days.add(Calendar.TUESDAY);
            }
            if (jsonObject.getBoolean(String.valueOf(Calendar.WEDNESDAY))) {
                days.add(Calendar.WEDNESDAY);
            }
            if (jsonObject.getBoolean(String.valueOf(Calendar.THURSDAY))) {
                days.add(Calendar.THURSDAY);
            }
            if (jsonObject.getBoolean(String.valueOf(Calendar.FRIDAY))) {
                days.add(Calendar.FRIDAY);
            }
            if (jsonObject.getBoolean(String.valueOf(Calendar.SATURDAY))) {
                days.add(Calendar.SATURDAY);
            }
            if (jsonObject.getBoolean(String.valueOf(Calendar.SUNDAY))) {
                days.add(Calendar.SUNDAY);
            }
            daysAsInt = days.stream().mapToInt(Integer::intValue).toArray();
            if (jsonObject.has("watchdogExceptionTimes")) {
                Object obj = jsonObject.get("watchdogExceptionTimes");
                if (obj instanceof JSONArray) {
                    for (Object json : (JSONArray)obj) {
                        exceptionTimes.add(TimeSpan.createTimeSpanFromJSONObject((JSONObject)json));
                    }
                } else if (obj instanceof JSONObject) {
                    exceptionTimes.add(TimeSpan.createTimeSpanFromJSONObject((JSONObject)obj));
                }
            }
        }
        return new WatchTimeExceptionData(daysAsInt, exceptionTimes);
    }

    /**
     * Copy method for WatchTimeExceptionData.
     *
     * @param data the data to be copied from
     * @return the new WatchTimeExceptionData
     */
    private WatchTimeExceptionData addWatchTimeExceptionData(WatchTimeExceptionData data) {
        if (data != null) {
            int[] daysAsInt = data.getDaysOfWeek();
            List<TimeSpan> exceptionTimes = new LinkedList<TimeSpan>();
            for (TimeSpan s : data.getTimesOfDay()) {
                Time newFromTime = new Time(s.getFrom().getHour(), s.getFrom().getMinute());
                Time newToTime = new Time(s.getTo().getHour(), s.getTo().getMinute());
                exceptionTimes.add(new TimeSpan(newFromTime, newToTime));
            }
            return new WatchTimeExceptionData(daysAsInt, exceptionTimes);
        } else {
            return null;
        }
    }


    /**
     * Constructs a config with default data.
     */
    @DataBoundConstructor
    public Config() {
        this(new JSONObject(false));
    }

    /**
     * Unused Constructor?
     *
     * @param formData the data
     * @param req      a path.
     */
    public Config(JSONObject formData, StaplerRequest req) {
        this(formData);
    }

    @Override
    public File getGerritAuthKeyFile() {
        return gerritAuthKeyFile;
    }

    /**
     * GerritAuthKeyFile.
     *
     * @param gerritAuthKeyFile the file
     * @see #getGerritAuthKeyFile()
     */
    public void setGerritAuthKeyFile(File gerritAuthKeyFile) {
        this.gerritAuthKeyFile = gerritAuthKeyFile;
    }

    @Override
    public String getGerritAuthKeyFilePassword() {
        return Secret.toString(gerritAuthKeyFilePassword);
    }

    /**
     * GerritAuthKeyFilePassword.
     *
     * @param gerritAuthKeyFilePassword the password
     * @see #getGerritAuthKeyFilePassword()
     */
    public void setGerritAuthKeyFilePassword(String gerritAuthKeyFilePassword) {
        this.gerritAuthKeyFilePassword = Secret.fromString(gerritAuthKeyFilePassword);
    }

    @Override
    public Secret getGerritAuthKeyFileSecretPassword() {
        return gerritAuthKeyFilePassword;
    }

    /**
     * Retrieves a configured gerrit label's vote value for a build status.
     * @param label gerrit label
     * @param status build status
     * @return vote value
     */
    @Override
    public Integer getLabelVote(String label, BuildStatus status) {
        for (VerdictCategory cat : categories) {
            if (cat.getVerdictValue().equals(label)) {
                return cat.getVerdictVote(status);
            }
        }
        return null;
    }

    /**
     * Sets a configured gerrit label's vote value for a build status.
     * @param label gerrit label
     * @param status build status
     * @param vote vote value
     */
    public void setLabelVote(String label, BuildStatus status, Integer vote) {
        for (VerdictCategory cat : categories) {
            if (cat.getVerdictValue().equals(label)) {
                cat.setVerdictVote(status, vote);
            }
        }
    }

    @Override
    public String getGerritFrontEndUrl() {
        String url = gerritFrontEndUrl;
        if (url != null && !url.isEmpty() && !url.endsWith("/")) {
            url += '/';
        }
        return url;
    }

    /**
     * GerritFrontEndURL.
     *
     * @param gerritFrontEndURL the URL
     * @see #getGerritFrontEndUrl()
     */
    public void setGerritFrontEndURL(String gerritFrontEndURL) {
        this.gerritFrontEndUrl = gerritFrontEndURL;
    }

    /**
     * This method is needed for configuration as code to discover this property.
     *
     * It would be ignored otherwise because of the "URL" vs. "Url" naming mismatch in old getter/setter.
     *
     * @param gerritFrontEndURL the Url
     */
    public void setGerritFrontEndUrl(String gerritFrontEndURL) {
        setGerritFrontEndURL(gerritFrontEndURL);
    }

    @Override
    public String getGerritHostName() {
        return gerritHostName;
    }

    /**
     * GerritHostName.
     *
     * @param gerritHostName the hostname
     * @see #getGerritHostName()
     */
    public void setGerritHostName(String gerritHostName) {
        this.gerritHostName = gerritHostName;
    }

    @Override
    public int getGerritSshPort() {
        return gerritSshPort;
    }

    /**
     * GerritSshPort.
     *
     * @param gerritSshPort the port
     * @see #getGerritSshPort()
     */
    public void setGerritSshPort(int gerritSshPort) {
        this.gerritSshPort = gerritSshPort;
    }

    @Override
    public String getGerritProxy() {
        return gerritProxy;
    }

    /**
     * GerritProxy.
     *
     * @param gerritProxy the proxy url
     * @see #getGerritProxy()
     */
    public void setGerritProxy(String gerritProxy) {
        this.gerritProxy = gerritProxy;
    }


    @Override
    public int getBuildScheduleDelay() {
        return buildScheduleDelay;
    }

    /**
     * Setting buildScheduleDelay.
     *
     * @param buildScheduleDelay the delay time
     * @see #getBuildScheduleDelay()
     */
    public void setBuildScheduleDelay(int buildScheduleDelay) {
        this.buildScheduleDelay = buildScheduleDelay;
    }

    @Override
    public int getDynamicConfigRefreshInterval() {
        if (dynamicConfigRefreshInterval == 0) {
            dynamicConfigRefreshInterval = DEFAULT_DYNAMIC_CONFIG_REFRESH_INTERVAL;
        } else if (dynamicConfigRefreshInterval < GerritDefaultValues.MINIMUM_DYNAMIC_CONFIG_REFRESH_INTERVAL) {
            dynamicConfigRefreshInterval = GerritDefaultValues.MINIMUM_DYNAMIC_CONFIG_REFRESH_INTERVAL;
        }
        return dynamicConfigRefreshInterval;
    }

    @Override
    public int getProjectListFetchDelay() {
        return projectListFetchDelay;
    }

    /**
     * Sets the delay from Jenkins startup before the project list should be fetched.
     * @param projectListFetchDelay the delay
     * @see #getProjectListFetchDelay()
     * @see #isEnableProjectAutoCompletion()
     */
    public void setProjectListFetchDelay(int projectListFetchDelay) {
        this.projectListFetchDelay = projectListFetchDelay;
    }

    @Override
    public int getProjectListRefreshInterval() {
        if (projectListRefreshInterval == 0) {
            projectListRefreshInterval = DEFAULT_PROJECT_LIST_REFRESH_INTERVAL;
        }
        return projectListRefreshInterval;
    }

    /**
     * The interval between recurrent fetches of the project list.
     * @param projectListRefreshInterval the interval
     * @see #getProjectListRefreshInterval()
     * @see #isEnableProjectAutoCompletion()
     */
    public void setProjectListRefreshInterval(int projectListRefreshInterval) {
        this.projectListRefreshInterval = projectListRefreshInterval;
    }

    @Override
    public boolean isEnableProjectAutoCompletion() {
        return enableProjectAutoCompletion;
    }

    /**
     * If the project list should be fetched from the gerrit server or not.
     *
     * @param enableProjectAutoCompletion true if so
     * @see #isEnableProjectAutoCompletion()
     * @see #getProjectListRefreshInterval()
     * @see #getProjectListFetchDelay()
     */
    public void setEnableProjectAutoCompletion(boolean enableProjectAutoCompletion) {
        this.enableProjectAutoCompletion = enableProjectAutoCompletion;
    }

    /**
     * Setting dynamicConfigRefreshInterval.
     *
     * @param dynamicConfigRefreshInterval the interval between the fetches.
     * @see #getDynamicConfigRefreshInterval()
     */
    public void setDynamicConfigRefreshInterval(int dynamicConfigRefreshInterval) {
        this.dynamicConfigRefreshInterval = dynamicConfigRefreshInterval;
    }

    @Override
    public String getGerritUserName() {
        return gerritUserName;
    }

    /**
     * GerritUserName.
     *
     * @param gerritUserName the username
     * @see #getGerritUserName()
     */
    public void setGerritUserName(String gerritUserName) {
        this.gerritUserName = gerritUserName;
    }

    @Override
    public String getGerritEMail() {
        return gerritEMail;
    }

    @Override
    public Notify getNotificationLevel() {
        return notificationLevel;
    }

    /**
     * The e-mail address for the user in gerrit.
     * Comments added from this e-mail address will be ignored.
     * @param gerritEMail the e-mail address.
     * @see #getGerritEMail()
     */
    public void setGerritEMail(String gerritEMail) {
        this.gerritEMail = gerritEMail;
    }

    /**
     * Sets the value for whom to notify.
     *
     * @param notificationLevel the notification level.
     */
    public void setNotificationLevel(Notify notificationLevel) {
        this.notificationLevel = notificationLevel;
    }

    @Override
    @Deprecated
    public int getNumberOfReceivingWorkerThreads() {
        if (numberOfWorkerThreads <= 0) {
            numberOfWorkerThreads = DEFAULT_NR_OF_RECEIVING_WORKER_THREADS;
        }
        return numberOfWorkerThreads;
    }

    @Override
    @Deprecated
    public int getNumberOfSendingWorkerThreads() {
        if (numberOfSendingWorkerThreads <= 0) {
            numberOfSendingWorkerThreads = DEFAULT_NR_OF_SENDING_WORKER_THREADS;
        }
        return numberOfSendingWorkerThreads;
    }

    /**
     * NumberOfWorkerThreads.
     *
     * @param numberOfReceivingWorkerThreads nr of threads.
     * @see #getNumberOfReceivingWorkerThreads()
     */
    @Deprecated
    public void setNumberOfReceivingWorkerThreads(int numberOfReceivingWorkerThreads) {
        this.numberOfWorkerThreads = numberOfReceivingWorkerThreads;
    }

    @Deprecated
    @Override
    public void setNumberOfSendingWorkerThreads(int numberOfSendingWorkerThreads) {
        this.numberOfSendingWorkerThreads = numberOfSendingWorkerThreads;
    }

    @Override
    public boolean isGerritBuildCurrentPatchesOnly() {
        return getBuildCurrentPatchesOnly().isEnabled();
    }

    @Override
    public BuildCancellationPolicy getBuildCurrentPatchesOnly() {
        if (this.buildCurrentPatchesOnly == null) {
            this.buildCurrentPatchesOnly = new BuildCancellationPolicy();
            this.buildCurrentPatchesOnly.setEnabled(false);
        }
        return this.buildCurrentPatchesOnly;
    }

    /**
     * Set build cancellation policy for current patches.
     *
     * Exposed for configuration as code.
     *
     * @param buildCurrentPatchesOnly new policy.
     */
    public void setBuildCurrentPatchesOnly(BuildCancellationPolicy buildCurrentPatchesOnly) {
        this.buildCurrentPatchesOnly = buildCurrentPatchesOnly;
    }

    @Override
    public boolean isVoteSameTopic() {
        return voteSameTopic;
    }

    /**
     * Standard setter for the voteSameTopic value.
     *
     * @param voteSameTopic true if patchsets with same topic should be voted together.
     */
    public void setVoteSameTopic(boolean voteSameTopic) {
        this.voteSameTopic = voteSameTopic;
    }

    /**
     * Returns the description message for the jelly entry.
     *
     * @return descriptionMsg for jelly.
     */
    public String getVoteSameTopicDescription() {
        return "<b>Warning:</b>"
                + " The current implementation takes into account that 'Build Current Patches Only'"
                + " with 'Abort new patch sets' and 'Abort patch sets with same topic' are <b>enabled</b>"
                + " (see help for more).";
    }

    @Override
    public String getGerritCmdBuildSuccessful() {
        return buildStatusCommandBuilderMap.get(BuildStatus.SUCCESSFUL).build();
    }

    /**
     * Set Gerrit Verified Cmd Build Successful.
     *
     * @param cmd the command
     * @see #getGerritCmdBuildSuccessful()
     * @deprecated use {@link Config#setGerritCmdBuildSuccessful(String) } instead.
     */
    @Deprecated
    public void setGerritVerifiedCmdBuildSuccessful(String cmd) {
        setGerritCmdBuildSuccessful(cmd);
    }

    /**
     * Set Gerrit Verified Cmd Build Successful.
     *
     * @param cmd the Gerrit command for successful verification
     * @see #getGerritCmdBuildSuccessful()
     */
    public void setGerritCmdBuildSuccessful(String cmd) {
        buildStatusCommandBuilderMap.put(BuildStatus.SUCCESSFUL, CommandBuilder.fromString(cmd));
    }

    @Override
    public String getGerritCmdBuildUnstable() {
        return buildStatusCommandBuilderMap.get(BuildStatus.UNSTABLE).build();
    }

    /**
     * Set Gerrit Verified Cmd Build Unstable.
     *
     * @param cmd the command
     * @see #getGerritCmdBuildUnstable()
     * @deprecated use {@link Config#setGerritCmdBuildUnstable(String) } instead.
     */
    @Deprecated
    public void setGerritVerifiedCmdBuildUnstable(String cmd) {
        setGerritCmdBuildUnstable(cmd);
    }

    /**
     * Set Gerrit Verified Cmd Build Unstable.
     *
     * @param cmd the command
     * @see #getGerritCmdBuildUnstable()
     */
    public void setGerritCmdBuildUnstable(String cmd) {
        buildStatusCommandBuilderMap.put(BuildStatus.UNSTABLE, CommandBuilder.fromString(cmd));
    }

    @Override
    public String getGerritCmdBuildFailed() {
        return buildStatusCommandBuilderMap.get(BuildStatus.FAILED).build();
    }

    /**
     * Set Gerrit Verified Cmd Build Failed.
     *
     * @param cmd the command
     * @see #getGerritCmdBuildFailed()
     * @deprecated use {@link Config#setGerritCmdBuildFailed(String) } instead.
     */
    @Deprecated
    public void setGerritVerifiedCmdBuildFailed(String cmd) {
        setGerritCmdBuildFailed(cmd);
    }

    /**
     * Set Gerrit Verified Cmd Build Failed.
     *
     * @param cmd the command
     * @see #getGerritCmdBuildFailed()
     */
    public void setGerritCmdBuildFailed(String cmd) {
        buildStatusCommandBuilderMap.put(BuildStatus.FAILED, CommandBuilder.fromString(cmd));
    }

    @Override
    public String getGerritCmdBuildStarted() {
        return buildStatusCommandBuilderMap.get(BuildStatus.STARTED).build();
    }

    /**
     * Set Gerrit Verified Cmd Build Started.
     *
     * @param cmd the command
     * @see #getGerritCmdBuildStarted()
     * @deprecated use {@link Config#setGerritCmdBuildStarted(String)} } instead.
     */
    @Deprecated
    public void setGerritVerifiedCmdBuildStarted(String cmd) {
        setGerritCmdBuildStarted(cmd);
    }

    /**
     * Set Gerrit Verified Cmd Build Started.
     *
     * @param cmd the command
     * @see #getGerritCmdBuildStarted()
     */
    public void setGerritCmdBuildStarted(String cmd) {
        buildStatusCommandBuilderMap.put(BuildStatus.STARTED, CommandBuilder.fromString(cmd));
    }

    @Override
    public String getGerritCmdBuildNotBuilt() {
        return buildStatusCommandBuilderMap.get(BuildStatus.NOT_BUILT).build();
    }

    /**
     * Set Gerrit Verified Cmd Build NotBuilt.
     *
     * @param cmd the command
     * @see #getGerritCmdBuildNotBuilt()
     * @deprecated use {@link Config#setGerritCmdBuildNotBuilt(String)} } instead.
     */
    @Deprecated
    public void setGerritVerifiedCmdBuildNotBuilt(String cmd) {
        setGerritCmdBuildNotBuilt(cmd);
    }

    /**
     * Set Gerrit Verified Cmd Build NotBuilt.
     *
     * @param cmd the command
     * @see #getGerritCmdBuildNotBuilt()
     */
    public void setGerritCmdBuildNotBuilt(String cmd) {
        buildStatusCommandBuilderMap.put(BuildStatus.NOT_BUILT, CommandBuilder.fromString(cmd));
    }

    @Override
    public String getGerritCmdBuildAborted() {
        return buildStatusCommandBuilderMap.get(BuildStatus.ABORTED).build();
    }

    /**
     * Set Gerrit Verified Cmd Build Aborted.
     *
     * @param cmd the command
     * @see #getGerritCmdBuildAborted()
     * @deprecated use {@link Config#setGerritCmdBuildAborted(String) }  instead.
     */
    @Deprecated
    public void setGerritVerifiedCmdBuildAborted(String cmd) {
        setGerritCmdBuildAborted(cmd);
    }

    /**
     * Set Gerrit Verified Cmd Build Aborted.
     *
     * @param cmd the command
     * @see #getGerritCmdBuildAborted()
     */
    public void setGerritCmdBuildAborted(String cmd) {
        buildStatusCommandBuilderMap.put(BuildStatus.ABORTED, CommandBuilder.fromString(cmd));
    }

    /**
     * @deprecated use {@link Config#getLabelVote(String, BuildStatus)} instead.
     */
    @Deprecated
    @Override
    public Integer getGerritBuildStartedVerifiedValue() { return getLabelVote(VERIFIED_LABEL, BuildStatus.STARTED); }

    /**
     * @deprecated use {@link Config#getLabelVote(String, BuildStatus)} instead.
     */
    @Deprecated
    @Override
    public Integer getGerritBuildStartedCodeReviewValue() { return getLabelVote(CODE_REVIEW_LABEL, BuildStatus.STARTED); }

    /**
     * @deprecated use {@link Config#getLabelVote(String, BuildStatus)} instead.
     */
    @Deprecated
    @Override
    public Integer getGerritBuildSuccessfulVerifiedValue() { return getLabelVote(VERIFIED_LABEL, BuildStatus.SUCCESSFUL); }

    /**
     * @deprecated use {@link Config#getLabelVote(String, BuildStatus)} instead.
     */
    @Deprecated
    @Override
    public Integer getGerritBuildSuccessfulCodeReviewValue() {
        return getLabelVote(CODE_REVIEW_LABEL, BuildStatus.SUCCESSFUL);
    }

    /**
     * @deprecated use {@link Config#getLabelVote(String, BuildStatus)} instead.
     */
    @Deprecated
    @Override
    public Integer getGerritBuildFailedVerifiedValue() {
        return getLabelVote(VERIFIED_LABEL, BuildStatus.FAILED);
    }

    /**
     * @deprecated use {@link Config#getLabelVote(String, BuildStatus)} instead.
     */
    @Deprecated
    @Override
    public Integer getGerritBuildFailedCodeReviewValue() {
        return getLabelVote(CODE_REVIEW_LABEL, BuildStatus.FAILED);
    }

    /**
     * @deprecated use {@link Config#getLabelVote(String, BuildStatus)} instead.
     */
    @Deprecated
    @Override
    public Integer getGerritBuildUnstableVerifiedValue() {
        return getLabelVote(VERIFIED_LABEL, BuildStatus.UNSTABLE);
    }

    /**
     * @deprecated use {@link Config#getLabelVote(String, BuildStatus)} instead.
     */
    @Deprecated
    @Override
    public Integer getGerritBuildUnstableCodeReviewValue() {
        return getLabelVote(CODE_REVIEW_LABEL, BuildStatus.UNSTABLE);
    }

    /**
     * @deprecated use {@link Config#getLabelVote(String, BuildStatus)} instead.
     */
    @Deprecated
    @Override
    public Integer getGerritBuildNotBuiltVerifiedValue() {
        return getLabelVote(VERIFIED_LABEL, BuildStatus.NOT_BUILT);
    }

    /**
     * @deprecated use {@link Config#getLabelVote(String, BuildStatus)} instead.
     */
    @Deprecated
    @Override
    public Integer getGerritBuildNotBuiltCodeReviewValue() {
        return getLabelVote(CODE_REVIEW_LABEL, BuildStatus.NOT_BUILT);
    }

    /**
     * @deprecated use {@link Config#getLabelVote(String, BuildStatus)} instead.
     */
    @Deprecated
    @Override
    public Integer getGerritBuildAbortedVerifiedValue() {
        return getLabelVote(VERIFIED_LABEL, BuildStatus.ABORTED);
    }

    /**
     * @deprecated use {@link Config#getLabelVote(String, BuildStatus)} instead.
     */
    @Deprecated
    @Override
    public Integer getGerritBuildAbortedCodeReviewValue() {
        return getLabelVote(CODE_REVIEW_LABEL, BuildStatus.ABORTED);
    }

    /**
     * Set Gerrit Build Started Verified Value.
     * @param gerritBuildStartedVerifiedValue value
     * @deprecated use {@link Config#setLabelVote(String, BuildStatus, Integer)}  instead.
     */
    @Deprecated
    public void setGerritBuildStartedVerifiedValue(Integer gerritBuildStartedVerifiedValue) {
        setLabelVote(VERIFIED_LABEL, BuildStatus.STARTED, gerritBuildStartedVerifiedValue);
    }

    /**
     * Set Gerrit Build Sucessful Verified Value.
     * @param gerritBuildSuccessfulVerifiedValue value
     * @deprecated use {@link Config#setLabelVote(String, BuildStatus, Integer)}  instead.
     */
    @Deprecated
    public void setGerritBuildSuccessfulVerifiedValue(Integer gerritBuildSuccessfulVerifiedValue) {
        setLabelVote(VERIFIED_LABEL, BuildStatus.SUCCESSFUL, gerritBuildSuccessfulVerifiedValue);
    }

    /**
     * Set Gerrit Build Failed Verified Value.
     * @param gerritBuildFailedVerifiedValue value
     * @deprecated use {@link Config#setLabelVote(String, BuildStatus, Integer)}  instead.
     */
    @Deprecated
    public void setGerritBuildFailedVerifiedValue(Integer gerritBuildFailedVerifiedValue) {
        setLabelVote(VERIFIED_LABEL, BuildStatus.FAILED, gerritBuildFailedVerifiedValue);
    }

    /**
     * Set Gerrit Build Unstable Verified Value.
     * @param gerritBuildUnstableVerifiedValue value
     * @deprecated use {@link Config#setLabelVote(String, BuildStatus, Integer)}  instead.
     */
    @Deprecated
    public void setGerritBuildUnstableVerifiedValue(Integer gerritBuildUnstableVerifiedValue) {
        setLabelVote(VERIFIED_LABEL, BuildStatus.UNSTABLE, gerritBuildUnstableVerifiedValue);
    }

    /**
     * Set Gerrit Build Not Build Verified Value.
     * @param gerritBuildNotBuiltVerifiedValue value
     * @deprecated use {@link Config#setLabelVote(String, BuildStatus, Integer)}  instead.
     */
    @Deprecated
    public void setGerritBuildNotBuiltVerifiedValue(Integer gerritBuildNotBuiltVerifiedValue) {
        setLabelVote(VERIFIED_LABEL, BuildStatus.NOT_BUILT, gerritBuildNotBuiltVerifiedValue);
    }

    /**
     * Set Gerrit Build Aborted Verified Value.
     * @param gerritBuildAbortedVerifiedValue value
     * @deprecated use {@link Config#setLabelVote(String, BuildStatus, Integer)}  instead.
     */
    @Deprecated
    public void setGerritBuildAbortedVerifiedValue(Integer gerritBuildAbortedVerifiedValue) {
        setLabelVote(VERIFIED_LABEL, BuildStatus.ABORTED, gerritBuildAbortedVerifiedValue);
    }

    /**
     * Set Gerrit Build Started Code Review Value.
     * @param gerritBuildStartedCodeReviewValue value
     * @deprecated use {@link Config#setLabelVote(String, BuildStatus, Integer)}  instead.
     */
    @Deprecated
    public void setGerritBuildStartedCodeReviewValue(Integer gerritBuildStartedCodeReviewValue) {
        setLabelVote(CODE_REVIEW_LABEL, BuildStatus.STARTED, gerritBuildStartedCodeReviewValue);
    }

    /**
     * Set Gerrit Build Successful Code Review Value.
     * @param gerritBuildSuccessfulCodeReviewValue value
     * @deprecated use {@link Config#setLabelVote(String, BuildStatus, Integer)}  instead.
     */
    @Deprecated
    public void setGerritBuildSuccessfulCodeReviewValue(Integer gerritBuildSuccessfulCodeReviewValue) {
        setLabelVote(CODE_REVIEW_LABEL, BuildStatus.SUCCESSFUL, gerritBuildSuccessfulCodeReviewValue);
    }

    /**
     * Set Gerrit Build Failed Code Review Value.
     * @param gerritBuildFailedCodeReviewValue value
     * @deprecated use {@link Config#setLabelVote(String, BuildStatus, Integer)}  instead.
     */
    @Deprecated
    public void setGerritBuildFailedCodeReviewValue(Integer gerritBuildFailedCodeReviewValue) {
        setLabelVote(CODE_REVIEW_LABEL, BuildStatus.FAILED, gerritBuildFailedCodeReviewValue);
    }

    /**
     * Set Gerrit Build Unstable Code Review Value.
     * @param gerritBuildUnstableCodeReviewValue value
     * @deprecated use {@link Config#setLabelVote(String, BuildStatus, Integer)}  instead.
     */
    @Deprecated
    public void setGerritBuildUnstableCodeReviewValue(Integer gerritBuildUnstableCodeReviewValue) {
        setLabelVote(CODE_REVIEW_LABEL, BuildStatus.UNSTABLE, gerritBuildUnstableCodeReviewValue);
    }

    /**
     * Set Gerrit Build Not Build Code Review Value.
     * @param gerritBuildNotBuiltCodeReviewValue value
     * @deprecated use {@link Config#setLabelVote(String, BuildStatus, Integer)}  instead.
     */
    @Deprecated
    public void setGerritBuildNotBuiltCodeReviewValue(Integer gerritBuildNotBuiltCodeReviewValue) {
        setLabelVote(CODE_REVIEW_LABEL, BuildStatus.NOT_BUILT, gerritBuildNotBuiltCodeReviewValue);
    }

    /**
     * Set Gerrit Build Aborted Code Review Value.
     * @param gerritBuildAbortedCodeReviewValue value
     * @deprecated use {@link Config#setLabelVote(String, BuildStatus, Integer)}  instead.
     */
    @Deprecated
    public void setGerritBuildAbortedCodeReviewValue(Integer gerritBuildAbortedCodeReviewValue) {
        setLabelVote(CODE_REVIEW_LABEL, BuildStatus.ABORTED, gerritBuildAbortedCodeReviewValue);
    }

    @Override
    public String getGerritFrontEndUrlFor(String changeSetNumber, String revision) {
        StringBuilder str = new StringBuilder(getGerritFrontEndUrl());
        str.append(changeSetNumber);
        return str.toString();
    }

    @Override
    public String getGerritFrontEndUrlFor(GerritTriggeredEvent event) {
        if (event instanceof ChangeBasedEvent) {
            String changeUrl = ((ChangeBasedEvent)event).getChange().getUrl();
            if (changeUrl != null && !changeUrl.isEmpty()) {
                return changeUrl;
            }
        }
        String url = getGerritFrontEndUrl();
        Provider provider = event.getProvider();
        if (provider != null) {
            String providerUrl = provider.getUrl();
            if (providerUrl != null && !providerUrl.isEmpty()) {
                url = providerUrl;
            }
        }
        StringBuilder str = new StringBuilder(url);
        if (event instanceof ChangeBasedEvent) {
            str.append(((ChangeBasedEvent)event).getChange().getNumber());
        }
        return str.toString();
    }

    @Override
    public List<VerdictCategory> getCategories() {
        return categories;
    }

    public Map<BuildStatus, CommandBuilder> getBuilderStatusCommandMap() { return buildStatusCommandBuilderMap; }

    /**
     * Setter for the list of VerdictCategories, used to make testing easier.
     * @param categories the list.
     */
    @Override
    public void setCategories(List<VerdictCategory> categories) {
        this.categories = categories;
    }

    /**
     * Getter for the enableManualTrigger value.
     * @return true if manual triggering is enabled.
     */
    @Override
    public boolean isEnableManualTrigger() {
        return enableManualTrigger;
    }

    /**
     * Sets if the manual trigger should be enabled or not.
     *
     * @param enableManualTrigger true if it should be enabled/shown.
     * @see #isEnableManualTrigger()
     */
    public void setEnableManualTrigger(boolean enableManualTrigger) {
        this.enableManualTrigger = enableManualTrigger;
    }

    @Override
    public Authentication getGerritAuthentication() {
        return new Authentication(gerritAuthKeyFile, gerritUserName, getGerritAuthKeyFilePassword());
    }

    @Override
    public boolean hasDefaultValues() {
        //both hostname and frontendurl should be null or "" for this to be true
        return (gerritHostName == null
                || (DEFAULT_GERRIT_HOSTNAME).equals(gerritHostName))

                && (gerritFrontEndUrl == null
                || (DEFAULT_GERRIT_HOSTNAME).equals(gerritFrontEndUrl));
    }

    @Override
    public boolean isEnablePluginMessages() {
        return enablePluginMessages;
    }

    @Override
    public boolean isTriggerOnAllComments() {
        return triggerOnAllComments;
    }

    /**
     * Sets triggerOnAllComments.
     *
     * @param triggerOnAllComments true if so
     * @see  #isTriggerOnAllComments()
     */
    public void setTriggerOnAllComments(boolean triggerOnAllComments) {
        this.triggerOnAllComments = triggerOnAllComments;
    }

    @Override
    public ReplicationConfig getReplicationConfig() {
        return replicationConfig;
    }

    /**
     * Set replication config.
     * @param replicationConfig The config
     */
    public void setReplicationConfig(ReplicationConfig replicationConfig) {
        this.replicationConfig = replicationConfig;
    }

    @Override
    public int getWatchdogTimeoutSeconds() {
        return (int)TimeUnit.MINUTES.toSeconds(watchdogTimeoutMinutes);
    }

    /**
     * Convenience getter for the jelly view.
     *
     * @return the watchdogTimeoutMinutes.
     */
    @Override
    public int getWatchdogTimeoutMinutes() {
        return watchdogTimeoutMinutes;
    }

    /**
     * Set watchdog timeout.
     * @param watchdogTimeoutMinutes Number of minutes.
     */
    public void setWatchdogTimeoutMinutes(int watchdogTimeoutMinutes) {
        this.watchdogTimeoutMinutes = watchdogTimeoutMinutes;
    }

    @Override
    public WatchTimeExceptionData getExceptionData() {
        return watchTimeExceptionData;
    }

    /**
     * Set exception data.
     * @param exceptionData The data.
     */
    public void setExceptionData(WatchTimeExceptionData exceptionData) {
        this.watchTimeExceptionData = exceptionData;
    }

    @Override
    public boolean isUseRestApi() {
        return useRestApi;
    }

    /**
     * Sets useRestApi.
     *
     * @param useRestApi true if so
     * @see #isUseRestApi()
     */
    public void setUseRestApi(boolean useRestApi) {
        this.useRestApi = useRestApi;
    }

    @Override
    public Secret getGerritHttpSecretPassword() {
        return gerritHttpPassword;
    }

    @Override
    public String getGerritHttpPassword() {
        return Secret.toString(gerritHttpPassword);
    }

    /**
     * Sets gerritHttpPassword.
     *
     * @param gerritHttpPassword the password
     * @see #getGerritHttpPassword()
     */
    public void setGerritHttpPassword(String gerritHttpPassword) {
        this.gerritHttpPassword = Secret.fromString(gerritHttpPassword);
    }

    @Override
    public String getGerritHttpUserName() {
        return gerritHttpUserName;
    }

    /**
     * Sets gerritHttpUserName.
     *
     * @param gerritHttpUserName the username
     * @see #getGerritHttpUserName()
     */
    public void setGerritHttpUserName(String gerritHttpUserName) {
        this.gerritHttpUserName = gerritHttpUserName;
    }

    @Override
    public Credentials getHttpCredentials() {
        return new UsernamePasswordCredentials(gerritHttpUserName, getGerritHttpPassword());
    }

    @Override
    public boolean isRestCodeReview() {
        return restCodeReview;
    }

    /**
     * Sets restCodeReview.
     * @param restCodeReview true if include Code-Review label to REST API for ReviewInput.
     */
    public void setRestCodeReview(boolean restCodeReview) {
        this.restCodeReview = restCodeReview;
    }

    @Override
    public boolean isRestVerified() {
        return restVerified;
    }

    /**
     * Sets restVerified.
     * @param restVerified true if include Verified label to REST API for ReviewInput.
     */
    public void setRestVerified(boolean restVerified) {
        this.restVerified = restVerified;
    }

    /**
     * When upgrading from an older version where buildCurrentPatchesOnly doesn't exist,
     * get the value from the now deprecated gerritBuildCurrentPatchesOnly.
     *
     * Secondly the possilbity to specific the behavour in case of an aborted build was not
     * present in earlier versions. For backward compatibility the values and therfore behaviour
     * of a failed build are used by default.
     *
     * @return the resolved instance.
     */
    Object readResolve() {
        if (this.buildCurrentPatchesOnly == null) {
            this.buildCurrentPatchesOnly = new BuildCancellationPolicy();
            this.buildCurrentPatchesOnly.setEnabled(gerritBuildCurrentPatchesOnly);
            this.buildCurrentPatchesOnly.setAbortManualPatchsets(false);
            this.buildCurrentPatchesOnly.setAbortNewPatchsets(false);
        }

        if (this.buildStatusCommandBuilderMap.get(BuildStatus.ABORTED) == null) {
            this.buildStatusCommandBuilderMap.put(BuildStatus.ABORTED, this.buildStatusCommandBuilderMap.get(BuildStatus.FAILED));

            /* Only set these values when dealnig with an old configuration */
            if (this.getGerritBuildAbortedCodeReviewValue() == null) {
                setLabelVote(CODE_REVIEW_LABEL, BuildStatus.ABORTED, this.getGerritBuildFailedCodeReviewValue());
            }

            if (this.getGerritBuildAbortedVerifiedValue() == null) {
                setLabelVote(VERIFIED_LABEL, BuildStatus.ABORTED, this.getGerritBuildFailedVerifiedValue());
            }
        }

        return this;
    }
}
