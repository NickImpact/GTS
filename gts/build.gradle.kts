plugins {
    id("org.spongepowered.gradle.vanilla") version("0.2.1-SNAPSHOT")
}

minecraft {
    version("1.16.5")
}

dependencies {
    implementation(project(":api"))

    api("net.impactdev.impactor.api:plugins:5.0.0-SNAPSHOT")

    implementation("redis.clients:jedis:2.10.2")
    implementation("com.squareup.okhttp3:okhttp:3.14.9")
    implementation("com.squareup.okio:okio:1.17.5")
    implementation("io.github.classgraph:classgraph:4.8.149")
}