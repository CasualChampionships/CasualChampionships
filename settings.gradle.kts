rootProject.name = "CasualChampionships"

include("minigames:uhc")
include("minigames:common")

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        mavenCentral()
        gradlePluginPortal()
    }

    val loom_version: String by settings
    val fabric_kotlin_version: String by settings
    plugins {
        id("com.github.johnrengelman.shadow") version "7.1.2"
        id("fabric-loom") version loom_version
        id("org.jetbrains.kotlin.jvm") version
                fabric_kotlin_version
                    .split("+kotlin.")[1] // Grabs the sentence after `+kotlin.`
                    .split("+")[0] // Ensures sentences like `+build.1` are ignored
    }
}