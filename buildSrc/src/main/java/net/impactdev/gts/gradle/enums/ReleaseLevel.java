package net.impactdev.gts.gradle.enums;

import java.util.function.Function;

public enum ReleaseLevel {

    SNAPSHOT(version -> version.endsWith("SNAPSHOT"), "snapshot", true),
    RELEASE_CANDIDATE(version -> version.contains("RC"), "release-candidate", false),
    MAJOR(version -> version.endsWith(".0.0"), "major", false),
    MINOR(version -> version.endsWith(".0"), "minor", false),
    PATCH(version -> true, "patch", false);

    private final Function<String, Boolean> validator;
    private final String template;
    private final boolean snapshot;

    ReleaseLevel(Function<String, Boolean> validator, String template, boolean snapshot) {
        this.validator = validator;
        this.template = template;
        this.snapshot = snapshot;
    }

    public Function<String, Boolean> getValidator() {
        return this.validator;
    }

    public String getTemplate() {
        return this.template;
    }

    public boolean isSnapshot() {
        return this.snapshot;
    }

}
