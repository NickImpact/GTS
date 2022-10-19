import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.apache.httpcomponents:httpmime:4.5.3")
        classpath("com.google.code.gson:gson:2.8.6")
    }
}

plugins {
    kotlin("jvm") version "1.7.20-RC"
    id("java")
}

repositories {
    mavenCentral()
    maven("https://maven.impactdev.net/repository/development/")
}

dependencies {
    gradleApi()
    implementation("net.impactdev:discord-webhook-api:1.0.1")
    implementation("org.apache.httpcomponents:httpmime:4.5.3")
    implementation("com.google.code.gson:gson:2.8.6")
}

tasks {
    withType(KotlinCompile::class) {
        this.kotlinOptions.jvmTarget = "17"
    }
}