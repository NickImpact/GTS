package net.impactdev.gts.gradle

import net.impactdev.gts.gradle.actions.GenerateChangelog
import net.impactdev.gts.gradle.enums.ReleaseLevel
import org.gradle.api.Project
import java.io.ByteArrayOutputStream

fun getGitHash(project: Project) : String {
    return try {
        val byteOut = ByteArrayOutputStream()
        project.exec {
            it.commandLine = "git rev-parse --short HEAD".split(" ")
            it.standardOutput = byteOut
        }

        byteOut.toString("UTF-8").trim()
    } catch (ex: Exception) {
        "Unknown"
    }
}

fun getLastCommitMessage(project: Project): String {
    return try {
        val byteOut = ByteArrayOutputStream()
        project.exec {
            it.commandLine = "git log -1 --format=%B".split(" ")
            it.standardOutput = byteOut
        }

        byteOut.toString("UTF-8").trim()
    } catch (ex: Exception) {
        "Unknown"
    }
}

fun generateChangelog(project: Project, level: ReleaseLevel): String {
    return GenerateChangelog(project,
        project.version.toString(),
        getGitHash(project),
        getLastCommitMessage(project),
        level
    ).generate()
}
