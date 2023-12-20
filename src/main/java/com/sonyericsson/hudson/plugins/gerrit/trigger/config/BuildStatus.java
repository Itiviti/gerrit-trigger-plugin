package com.sonyericsson.hudson.plugins.gerrit.trigger.config;

import hudson.model.Result;
import hudson.plugins.git.util.Build;

public enum BuildStatus {
    STARTED,
    SUCCESSFUL,
    FAILED,
    UNSTABLE,
    NOT_BUILT,
    ABORTED,
    NOT_REGISTERED;

    public static BuildStatus fromResult(Result result) {
        if (result == Result.SUCCESS)
            return BuildStatus.SUCCESSFUL;
        if (result == Result.FAILURE)
            return BuildStatus.FAILED;
        if (result == Result.UNSTABLE)
            return BuildStatus.UNSTABLE;
        if (result == Result.NOT_BUILT)
            return BuildStatus.NOT_BUILT;
        if (result == Result.ABORTED)
            return BuildStatus.ABORTED;

        return BuildStatus.FAILED;
    }
}
