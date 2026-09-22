pluginManagement {
    includeBuild("build-logic")

    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

rootProject.name = "CasualChampionships"

include(
    ":minigames-common",
    ":minigames-duels",
    ":minigames-lobby",
    ":minigames-missile-wars",
    ":minigames-uhc",
)
