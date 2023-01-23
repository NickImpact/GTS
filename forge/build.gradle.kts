plugins {
    id("architectury-plugin")
    id("dev.architectury.loom")
}

architectury {
    platformSetupLoomIde()
    forge()
}

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${rootProject.property("minecraft")}")
    mappings(loom.officialMojangMappings())
    forge("net.minecraftforge:forge:${rootProject.property("minecraft")}-${rootProject.property("forge")}")

    implementation(project(":api"))
    implementation(project(":gts"))

    modRuntimeOnly("net.impactdev.impactor.launchers:forge:5.0.0-SNAPSHOT") {
        exclude("net.impactdev.impactor.api")
        exclude("net.impactdev.impactor.common", "impactor")
        exclude("net.impactdev.impactor.game", "game")
        exclude(mapOf("group" to "net.impactdev.impactor.launchers"))
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks {
    shadowJar {
        dependencies {
            include(project(":api"))
            include(project(":gts"))

            include(dependency("aopalliance:aopalliance:.*"))
            include(dependency("javax.inject:javax.inject:.*"))
            include(dependency("com.google.inject:guice:.*"))
            include(dependency("com.squareup.okhttp3:okhttp:.*"))
            include(dependency("com.squareup.okio:okio:.*"))

            exclude("forge-client-extra.jar")
        }

        relocate ("io.github.classgraph", "net.impactdev.impactor.relocations.classgraph")
        relocate("org.aopalliance", "net.impactdev.gts.relocations.aopalliance")
        relocate("com.google.inject", "net.impactdev.gts.relocations.google.inject")
        relocate("javax.inject", "net.impactdev.gts.relocations.javax.inject")
        relocate("net.kyori", "net.impactdev.impactor.relocations.kyori")
        relocate("cloud", "net.impactdev.impactor.relocations.cloud")
        relocate("com.github.benmanes.caffeine", "net.impactdev.impactor.relocations.caffeine")
        relocate("okhttp3", "net.impactdev.gts.relocations.okhttp3")
        relocate("okio", "net.impactdev.gts.relocations.okio")
        relocate("org.spongepowered.configurate", "net.impactdev.impactor.relocations.configurate")
        relocate ("org.spongepowered.math", "net.impactdev.impactor.relocations.spongepowered.math")
    }

    remapJar {
        val minecraft = rootProject.property("minecraft")
        val forge = rootProject.property("forge")

        archiveBaseName.set("GTS-Forge")
        archiveClassifier.set("")
        archiveVersion.set("$minecraft-$forge-${rootProject.version}")

        dependsOn(shadowJar)
        inputFile.set(named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar").flatMap { it.archiveFile })
    }

    processResources {
        inputs.property("version", rootProject.version)

        filesMatching("META-INF/mods.toml") {
            expand("version" to rootProject.version)
        }
    }
}