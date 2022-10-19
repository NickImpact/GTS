pluginManagement {
    repositories {
        maven(url = "https://repo.spongepowered.org/repository/maven-public/")
        maven(url = "https://maven.fabricmc.net/")
        maven(url = "https://maven.architectury.dev/")
        maven(url = "https://maven.minecraftforge.net/")
        gradlePluginPortal()
    }
}

rootProject.name = "GTS"

include("api")
include("gts")
