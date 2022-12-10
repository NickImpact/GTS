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

            exclude("forge-client-extra.jar")
        }

        relocate("org.aopalliance", "net.impactdev.gts.relocations.aopalliance")
        relocate("com.google.inject", "net.impactdev.gts.relocations.google.inject")
        relocate("javax.inject", "net.impactdev.gts.relocations.javax.inject")
        relocate ("net.kyori", "net.impactdev.impactor.relocations.kyori")
        relocate ("cloud", "net.impactdev.impactor.relocations.cloud")
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