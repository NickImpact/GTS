import net.impactdev.gts.gradle.enums.ReleaseLevel
import net.impactdev.gts.gradle.generateChangelog

import java.nio.file.Files
import java.nio.charset.StandardCharsets

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.google.code.gson:gson:2.9.0")
    }
}

plugins {
    base
    id("java")
    id("java-library")
    id("org.cadixdev.licenser") version "0.6.1"
    id("net.kyori.blossom") version "1.3.0" apply false
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT" apply false
    id("dev.architectury.loom") version "0.12.0-SNAPSHOT" apply false
}

group = "net.impactdev.gts"
version = "7.0.0-SNAPSHOT"

if(System.getenv().containsKey("BUILD_NUMBER")) {
    project.setProperty("run", System.getenv("BUILD_NUMBER"))
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "net.kyori.blossom")

    version = "${project.version}${"-" + (project.findProperty("BUILD_NUMBER")?: "")}"

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    }

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.impactdev.net/repository/development/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }

    dependencies {
        // Impactor
        api("net.impactdev.impactor:api:5.0.0-SNAPSHOT")

        // Google
        api("com.google.inject:guice:5.1.0")

        // Misc
        api(group = "org.mariuszgromada.math", name = "MathParser.org-mXparser", version = "5.0.6")
        implementation("com.github.ben-manes.caffeine:caffeine:2.9.3")

        // Testing
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")

        testImplementation("com.google.guava:guava:31.1-jre")
        testImplementation(group = "org.mariuszgromada.math", name = "MathParser.org-mXparser", version = "5.0.6")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

}

val writeChangelog by tasks.registering {
    val path = project.projectDir.toPath().resolve("$buildDir").resolve("deploy").resolve("${project.version}.md")
    if(!Files.exists(path)) {
        Files.createDirectories(path.parent)
        Files.createFile(path)
    }

    Files.write(path, generateChangelog(project, ReleaseLevel.get(project.version.toString())).toByteArray(StandardCharsets.UTF_8))
}

tasks.withType<Test> {
    useJUnitPlatform()
}