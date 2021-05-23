/* This design originates from Nucleus, which is licensed under MIT */
package net.impactdev.gts.gradle.enums

import java.util.function.Predicate

enum class ReleaseLevel(val validator: Predicate<String>, val template: String, val isSnapshot: Boolean) {
    SNAPSHOT(Predicate { version: String -> version.endsWith("SNAPSHOT") }, "snapshot", true), RELEASE_CANDIDATE(
        Predicate { version: String -> version.contains("RC") }, "release-candidate", false
    ),
    MAJOR(
        Predicate { version: String -> version.endsWith(".0.0") }, "major", false
    ),
    MINOR(
        Predicate { version: String -> version.endsWith(".0") }, "minor", false
    ),
    PATCH(
        Predicate<String> { version: String? -> true }, "patch", false
    );

    companion object {
        operator fun get(version: String): ReleaseLevel {
            for (level in values()) {
                if (level.validator.test(version)) {
                    return level
                }
            }
            return SNAPSHOT
        }
    }
}