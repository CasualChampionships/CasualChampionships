rootProject.name = "CasualChampionships"

include(":minigames-common")
include(":minigames-duels")
include(":minigames-missile-wars")
include(":minigames-uhc")

pluginManagement {
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
