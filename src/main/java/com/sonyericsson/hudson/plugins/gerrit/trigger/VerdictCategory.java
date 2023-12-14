/*
 *  The MIT License
 *
 *  Copyright 2012 Sony Mobile Communications AB. All rights reserved.
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
package com.sonyericsson.hudson.plugins.gerrit.trigger;

import com.sonyericsson.hudson.plugins.gerrit.trigger.config.BuildStatus;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

import static com.sonyericsson.hudson.plugins.gerrit.trigger.utils.StringUtil.*;

/**
 * A verdict category for setting comments in Gerrit, i.e. code-review, verify
 * @author Tomas Westling &lt;tomas.westling@sonymobile.com&gt;
 */
public class VerdictCategory extends AbstractDescribableImpl<VerdictCategory> implements Cloneable {

    private final String verdictValue;
    private final String verdictDescription;
    private Integer buildStartedVote;
    private Integer buildSuccessfulVote;
    private Integer buildFailedVote;
    private Integer buildUnstableVote;
    private Integer buildNotBuiltVote;
    private Integer buildAbortedVote;

    /**
     * Standard constructor
     * @param verdictValue the value in Gerrit for the verdict category.
     * @param verdictDescription the text describing the verdict category.
     * @param buildStartedVote the vote value for when the build has started.
     * @param buildSuccessfulVote the vote value for when the build has been successful.
     * @param buildFailedVote the vote value for when the build has failed.
     * @param buildUnstableVote the vote value for when the build has been unstable.
     * @param buildNotBuiltVote the vote value for when the build has not been built.
     */
    @DataBoundConstructor
    public VerdictCategory(String verdictValue,
                           String verdictDescription,
                           Integer buildStartedVote,
                           Integer buildSuccessfulVote,
                           Integer buildFailedVote,
                           Integer buildUnstableVote,
                           Integer buildNotBuiltVote,
                           Integer buildAbortedVote) {
        this(verdictValue, verdictDescription);

        this.buildStartedVote = buildStartedVote;
        this.buildSuccessfulVote = buildSuccessfulVote;
        this.buildFailedVote = buildFailedVote;
        this.buildUnstableVote = buildUnstableVote;
        this.buildNotBuiltVote = buildNotBuiltVote;
        this.buildAbortedVote = buildAbortedVote;
    }

    /**
     * Standard constructor
     * @param verdictValue the value in Gerrit for the verdict category.
     * @param verdictDescription the text describing the verdict category.
     */
    public VerdictCategory(String verdictValue,
                           String verdictDescription) {
        this.verdictValue = verdictValue;
        this.verdictDescription = verdictDescription;
    }

    /**
     * Creates a VerdictCategory from a JSONObject.
     * @param obj the JSONObject.
     * @return a VerdictCategory.
     */
    public static VerdictCategory fromJSON(JSONObject obj) {
        String value = obj.getString("verdictValue");
        String description = obj.getString("verdictDescription");

        Integer buildStartedVote = getValueFromFormData(obj, "buildStartedVote");
        Integer buildSuccessfulVote = getValueFromFormData(obj, "buildSuccessfulVote");
        Integer buildFailedVote = getValueFromFormData(obj, "buildFailedVote");
        Integer buildUnstableVote = getValueFromFormData(obj, "buildUnstableVote");
        Integer buildNotBuiltVote = getValueFromFormData(obj, "buildNotBuiltVote");
        Integer buildAbortedVote = getValueFromFormData(obj, "buildAbortedVote");

        return new VerdictCategory(value,
                description,
                buildStartedVote,
                buildSuccessfulVote,
                buildFailedVote,
                buildUnstableVote,
                buildNotBuiltVote,
                buildAbortedVote);
    }

    /**
     * Standard getter for the value.
     * @return the value.
     */
    public String getVerdictValue() {
        return verdictValue;
    }

    /**
     * Standard getter for the description.
     * @return the description.
     */
    public String getVerdictDescription() {
        return verdictDescription;
    }

    public Integer getBuildStartedVote() {
        return buildStartedVote;
    }

    public void setBuildStartedVote(Integer buildStartedVote) {
        this.buildStartedVote = buildStartedVote;
    }

    public Integer getBuildSuccessfulVote() {
        return buildSuccessfulVote;
    }

    public void setBuildSuccessfulVote(Integer buildSuccessfulVote) {
        this.buildSuccessfulVote = buildSuccessfulVote;
    }

    public Integer getBuildFailedVote() {
        return buildFailedVote;
    }

    public void setBuildFailedVote(Integer buildFailedVote) {
        this.buildFailedVote = buildFailedVote;
    }

    public Integer getBuildUnstableVote() {
        return buildUnstableVote;
    }

    public void setBuildUnstableVote(Integer buildUnstableVote) {
        this.buildUnstableVote = buildUnstableVote;
    }

    public Integer getBuildNotBuiltVote() {
        return buildNotBuiltVote;
    }

    public void setBuildNotBuiltVote(Integer buildNotBuiltVote) {
        this.buildNotBuiltVote = buildNotBuiltVote;
    }

    public Integer getBuildAbortedVote() {
        return buildAbortedVote;
    }

    public void setBuildAbortedVote(Integer buildAbortedVote) {
        this.buildAbortedVote = buildAbortedVote;
    }

    @Override
    public VerdictCategory clone() {
            return new VerdictCategory(verdictValue,
                    verdictDescription,
                    buildStartedVote,
                    buildSuccessfulVote,
                    buildFailedVote,
                    buildUnstableVote,
                    buildNotBuiltVote,
                    buildAbortedVote);
    }

    public Integer getVerdictVote(BuildStatus status) {
        switch (status) {
            case STARTED:
                return buildStartedVote;
            case SUCCESSFUL:
                return buildSuccessfulVote;
            case FAILED:
                return buildFailedVote;
            case UNSTABLE:
                return buildUnstableVote;
            case NOT_BUILT:
                return buildNotBuiltVote;
            case ABORTED:
                return buildAbortedVote;
            default:
                return null;
        }
    }

    public void setVerdictVote(BuildStatus status, Integer vote) {
        switch (status) {
            case STARTED:
                buildStartedVote = vote;
                break;
            case SUCCESSFUL:
                buildSuccessfulVote = vote;
                break;
            case FAILED:
                buildFailedVote = vote;
                break;
            case UNSTABLE:
                buildUnstableVote = vote;
                break;
            case NOT_BUILT:
                buildNotBuiltVote = vote;
                break;
            case ABORTED:
                buildAbortedVote = vote;
                break;
            default:
                break;
        }
    }


    /**
     * The Descriptor for a VerdictCategory.
     */
    @Extension
    public static class VerdictCategoryDescriptor extends Descriptor<VerdictCategory> {
        @Override
        public String getDisplayName() {
            return "";
        }
    }
}
