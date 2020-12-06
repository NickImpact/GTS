/* This design originates from Nucleus, which is licensed under MIT */
package net.impactdev.gts.gradle.enums;

import java.util.function.Predicate;

public enum ReleaseLevel {

    SNAPSHOT(version -> version.endsWith("SNAPSHOT"), "snapshot", true),
    RELEASE_CANDIDATE(version -> version.contains("RC"), "release-candidate", false),
    MAJOR(version -> version.endsWith(".0.0"), "major", false),
    MINOR(version -> version.endsWith(".0"), "minor", false),
    PATCH(version -> true, "patch", false);

    private final Predicate<String> validator;
    private final String template;
    private final boolean snapshot;

    ReleaseLevel(Predicate<String> validator, String template, boolean snapshot) {
        this.validator = validator;
        this.template = template;
        this.snapshot = snapshot;
    }

    public Predicate<String> getValidator() {
        return this.validator;
    }

    public String getTemplate() {
        return this.template;
    }

    public boolean isSnapshot() {
        return this.snapshot;
    }

    public static ReleaseLevel get(String version) {
        for(ReleaseLevel level : ReleaseLevel.values()) {
            if(level.getValidator().test(version)) {
                return level;
            }
        }

        return ReleaseLevel.SNAPSHOT;
    }

}
