plugins {
    id("org.spongepowered.gradle.vanilla") version("0.2.1-SNAPSHOT")
}

minecraft {
    version("1.16.5")
}

dependencies {
    implementation(project(":api"))
    implementation("redis.clients:jedis:2.10.2")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
}