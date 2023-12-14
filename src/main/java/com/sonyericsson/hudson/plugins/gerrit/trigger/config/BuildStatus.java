package com.sonyericsson.hudson.plugins.gerrit.trigger.config;

public enum BuildStatus {
    STARTED,
    SUCCESSFUL,
    FAILED,
    UNSTABLE,
    NOT_BUILT,
    ABORTED,
    NOT_REGISTERED;
}
