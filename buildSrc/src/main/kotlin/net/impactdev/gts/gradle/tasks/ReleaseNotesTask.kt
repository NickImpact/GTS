/* This design originates from Nucleus, which is licensed under MIT */
package net.impactdev.gts.gradle.tasks

import net.impactdev.gts.gradle.enums.ReleaseLevel
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import java.io.BufferedReader
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import java.util.function.Function
import java.util.regex.Pattern

class ReleaseNotesTask : DefaultTask() {
    var result = "No templated release notes available"
    var version: String? = null
    var hash: String? = null
    var spongeVersion: String? = null
    var message: String? = null
    var sinceLastRelease: String? = null
    var level: ReleaseLevel? = null
    private val shortHash: String
        private get() = hash!!.substring(0, 7)

    @TaskAction
    fun run() {
        val template = project.rootDir.toPath()
            .resolve("changelogs")
            .resolve("templates")
            .resolve(level.getTemplate() + ".md")
        if (Files.exists(template)) {
            try {
                val result = read(Files.newBufferedReader(template))
                this.result = Placeholders.transform(this, result)
            } catch (e: Exception) {
                throw GradleException(e.message)
            }
        }
    }

    private val changesForVersion: Optional<String>
        private get() {
            val notes = project.rootDir.toPath()
                .resolve("changelogs")
                .resolve(version + ".md")
            return if (Files.exists(notes)) {
                try {
                    Optional.of(read(Files.newBufferedReader(notes, StandardCharsets.UTF_8)))
                } catch (e: Exception) {
                    e.printStackTrace()
                    Optional.empty<String>()
                }
            } else Optional.empty()
        }

    @Throws(IOException::class)
    private fun read(reader: BufferedReader): String {
        val result = StringBuilder()
        var working: String?
        while (reader.readLine().also { working = it } != null) {
            result.append(working).append('\n')
        }
        return result.toString()
    }

    private enum class Placeholders(val key: String, val replacement: Function<ReleaseNotesTask, String?>) {
        VERSION("version", Function { obj: ReleaseNotesTask -> obj.version }), HASH(
            "commit-hash",
            Function { obj: ReleaseNotesTask -> obj.hash }),
        SHORT_HASH(
            "commit-hash-short",
            Function<ReleaseNotesTask, String?> { obj: ReleaseNotesTask -> obj.shortHash }),
        MESSAGE("commit-message", Function { obj: ReleaseNotesTask -> obj.message }), SPONGE(
            "sponge",
            Function { obj: ReleaseNotesTask -> obj.spongeVersion }),
        CHANGES("changes", Function { task: ReleaseNotesTask ->
            val result = task.changesForVersion
            result.orElse("No release notes available for this version...")
        }),
        SINCE_LAST_RELEASE("since-last-release", Function<ReleaseNotesTask, String?> { task: ReleaseNotesTask ->
            val pattern = Pattern.compile("(?<commit>[0-9a-z]+)( [(].+[)])? (?<message>.+)")
            val matcher = pattern.matcher(task.sinceLastRelease)
            val joiner = StringJoiner("\n")
            while (matcher.find()) {
                var commit = matcher.group("commit")
                commit = "[" + commit.substring(0, 7) + "](https://github.com/NickImpact/GTS/commit/" + commit + ")"
                joiner.add(commit + " " + matcher.group("message") + "  ")
            }
            joiner.toString()
        });

        companion object {
            fun transform(task: ReleaseNotesTask, `in`: String): String {
                var out = `in`
                for (placeholder in values()) {
                    out = out.replace("{{" + placeholder.key + "}}", placeholder.replacement.apply(task))
                }
                return out
            }
        }
    }
}