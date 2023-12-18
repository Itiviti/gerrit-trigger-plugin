package com.sonyericsson.hudson.plugins.gerrit.trigger.config;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
    * Builder for gerrit review commands.
    * @author schemuwel &lt;schemuwel@gmail.com&gt;
 */
public class CommandBuilder {
    private Map<String, String> labels = new HashMap<>(Map.of(
            Constants.CODE_REVIEW_LABEL.toLowerCase(), "<CODE_REVIEW>",
            Constants.VERIFIED_LABEL.toLowerCase(), "<VERIFIED>"
    ));

    private String command = "gerrit review";
    private String message;
    private String change = "<CHANGE>";
    private String patchset = "<PATCHSET>";

    public CommandBuilder WithChange(String change) {
        this.change = change;
        return this;
    }

    public CommandBuilder WithPatchset(String patchset) {
        this.patchset = patchset;
        return this;
    }

    public CommandBuilder WithMessage(String message) {
        this.message = message;
        return this;
    }

    public CommandBuilder WithLabel(String label) {
        String placeholderLabelValue = String.format("<%s>", label.toUpperCase().replace("-", "_"));
        this.labels.put(label.toLowerCase(), placeholderLabelValue);
        return this;
    }

    public CommandBuilder WithLabel(String label, String labelValue) {
        this.labels.put(label.toLowerCase(), labelValue);
        return this;
    }

    public static CommandBuilder fromString(String command, CommandBuilder defaultCommandBuilder) {
        if (command == null) {
            return defaultCommandBuilder;
        }

        return fromString(command);
    }

    public static CommandBuilder fromString(String command) {
        Pattern gerritReviewPattern = Pattern.compile("(\\S+),(\\S+)");
        Pattern messagePattern = Pattern.compile("--message '(.*?)'");
        Pattern labelPattern = Pattern.compile("--(?!message|tag\\b)(\\S+) (\\S+)");

        Matcher gerritReviewMatcher = gerritReviewPattern.matcher(command);
        Matcher messageMatcher = messagePattern.matcher(command);
        Matcher labelMatcher = labelPattern.matcher(command);

        CommandBuilder commandBuilder = new CommandBuilder();
        if (gerritReviewMatcher.find()) {
            String change = gerritReviewMatcher.group(1);
            String patchset = gerritReviewMatcher.group(2);

            commandBuilder.WithChange(change).WithPatchset(patchset);
        }

        if (messageMatcher.find()) {
            String message = messageMatcher.group(1);

            commandBuilder.WithMessage(message);
        }

        while (labelMatcher.find()) {
            String label = labelMatcher.group(1);
            if (commandBuilder.getLabels().containsKey(label)) {
                continue;
            }

            commandBuilder.WithLabel(label);
        }

        return commandBuilder;
    }

    private Map<String, String> getLabels() {
        return labels;
    }

    public String build() {
        StringBuilder gerritCommand = new StringBuilder(command);
        gerritCommand.append(String.format(" %s,%s", change, patchset));
        gerritCommand.append(String.format(" --message '%s'", message));
        for (Map.Entry<String, String> label : labels.entrySet()) {
            gerritCommand.append(String.format(" --%s %s", label.getKey(), label.getValue()));
        }
        gerritCommand.append(String.format(" --tag %s", Constants.TAG_VALUE));
        return gerritCommand.toString();
    }
}
