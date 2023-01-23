plugins {
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    // Event Generation
    annotationProcessor("org.spongepowered:event-impl-gen:8.0.0-SNAPSHOT")
    compileOnlyApi("org.spongepowered:event-impl-gen-annotations:8.0.0-SNAPSHOT")

    testImplementation(group = "junit", name = "junit", version = "4.13.1")
}

tasks {
    withType<JavaCompile> {
        options.compilerArgs.add("-AeventGenFactory=net.impactdev.gts.api.events.factory.GTSEventFactory")
    }
}

publishing {
    repositories {
        maven("https://maven.impactdev.net/repository/development/") {
            name = "ImpactDev-Public"
            credentials {
                username = System.getenv("NEXUS_USER")
                password = System.getenv("NEXUS_PW")
            }
        }
    }

    publications {
        create<MavenPublication>("api") {
            from(components["java"])

            groupId = "net.impactdev.impactor"
            artifactId = "api"
            version = rootProject.version.toString()
        }
    }
}