rootProject.name = "CasualChampionships"

include("minigames:uhc")
include("minigames:missile_wars")
include("minigames:common")
include("minigames:duel")

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
