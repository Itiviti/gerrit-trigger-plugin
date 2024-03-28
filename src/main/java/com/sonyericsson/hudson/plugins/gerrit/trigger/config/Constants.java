/*
 *  The MIT License
 *
 *  Copyright 2013 Jyrki Puttonen. All rights reserved.
 *  Copyright 2013 Sony Mobile Communications AB. All rights reserved.
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

import com.sonymobile.tools.gerrit.gerritevents.dto.rest.Notify;

/**
* Global constants.
*/
public final class Constants {
    /**
     * Tag value sent to Gerrit for review comments.
     */
    public static final String TAG_VALUE = "autogenerated:jenkins-gerrit-trigger";

    /**
     * Code-Review review label
     */
    public static final String CODE_REVIEW_LABEL = "Code-Review";

    /**
     * Verified review label
     */
    public static final String VERIFIED_LABEL = "Verified";

    public static final Notify DEFAULT_NOTIFICATION_LEVEL = Notify.ALL;

    public static final String GERRIT_CMD_BUILD_STARTED_DEFAULT_VALUE = "gerrit review <CHANGE>,<PATCHSET> "
            + "--message 'Build Started <BUILDURL> <STARTED_STATS>' "
            + "--verified <VERIFIED> --code-review <CODE_REVIEW> --tag " + Constants.TAG_VALUE;
    public static final String GERRIT_CMD_BUILD_SUCCESSFUL_DEFAULT_VALUE = "gerrit review <CHANGE>,<PATCHSET> "
            + "--message 'Build Successful <BUILDS_STATS>' "
            + "--verified <VERIFIED> --code-review <CODE_REVIEW> --tag " + Constants.TAG_VALUE;
    public static final String GERRIT_CMD_BUILD_FAILED_DEFAULT_VALUE = "gerrit review <CHANGE>,<PATCHSET> "
            + "--message 'Build Failed <BUILDS_STATS>' "
            + "--verified <VERIFIED> --code-review <CODE_REVIEW> --tag " + Constants.TAG_VALUE;
    public static final String GERRIT_CMD_BUILD_UNSTABLE_DEFAULT_VALUE = "gerrit review <CHANGE>,<PATCHSET> "
            + "--message 'Build Unstable <BUILDS_STATS>' "
            + "--verified <VERIFIED> --code-review <CODE_REVIEW> --tag " + Constants.TAG_VALUE;
    public static final String GERRIT_CMD_BUILD_NOT_BUILT_DEFAULT_VALUE = "gerrit review <CHANGE>,<PATCHSET> "
            + "--message 'No Builds Executed <BUILDS_STATS>' "
            + "--verified <VERIFIED> --code-review <CODE_REVIEW> --tag " + Constants.TAG_VALUE;
    public static final String GERRIT_CMD_BUILD_ABORTED_DEFAULT_VALUE = "gerrit review <CHANGE>,<PATCHSET> "
            + "--message 'Build Aborted <BUILDS_STATS>' "
            + "--verified <VERIFIED> --code-review <CODE_REVIEW> --tag " + Constants.TAG_VALUE;
    /** Internal */
    private Constants() {
    }
}
