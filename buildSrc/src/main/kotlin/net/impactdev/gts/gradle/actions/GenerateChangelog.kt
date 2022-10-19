package net.impactdev.gts.gradle.actions

import net.impactdev.gts.gradle.enums.ReleaseLevel
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.BufferedReader
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.function.Function

class GenerateChangelog(val project: Project, val version: String, val hash: String, val message: String, val level: ReleaseLevel) {

    public val changes = getChangesForVersion(project)

    public fun generate(): String {
        val template: Path = this.project.projectDir.toPath()
            .resolve("changelogs")
            .resolve("templates")
            .resolve(this.level.template + ".md")

        return if (Files.exists(template)) {
            try {
                val result: String = read(Files.newBufferedReader(template))
                return Placeholders.transform(this, result)
            } catch (e: Exception) {
                throw GradleException(e.message!!)
            }
        } else {
            "Unknown"
        }
    }

    fun getShortHash(): String {
        return hash.substring(0, 7)
    }

    @Throws(IOException::class)
    private fun read(reader: BufferedReader): String {
        val result = StringBuilder()
        reader.readLines().forEach {
            result.append(it).append('\n')
        }
        return result.toString()
    }

    private fun getChangesForVersion(project: Project): Optional<String> {
        val notes: Path = project.projectDir.toPath()
            .resolve("changelogs")
            .resolve(this.version + ".md")
        return if (Files.exists(notes)) {
            try {
                Optional.of(read(Files.newBufferedReader(notes, StandardCharsets.UTF_8)))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Optional.empty()
            }
        } else Optional.empty()
    }

}

private enum class Placeholders(val key: String, val replacement: Function<GenerateChangelog, String>) {
    VERSION("version", Function { obj: GenerateChangelog -> obj.version }),
    HASH("commit-hash", Function { obj: GenerateChangelog -> obj.hash }),
    SHORT_HASH("commit-hash-short", Function { obj: GenerateChangelog -> obj.getShortHash() }),
    MESSAGE("commit-message", Function { obj: GenerateChangelog -> obj.message }),
    CHANGES("changes", Function { task: GenerateChangelog ->
        val result = task.changes
        result.orElse("No release notes available for this version...")
    });

    companion object {
        fun transform(task: GenerateChangelog, `in`: String): String {
            var out = `in`
            for (placeholder in Placeholders.values()) {
                out = out.replace("{{" + placeholder.key + "}}", placeholder.replacement.apply(task))
            }
            return out
        }
    }
}