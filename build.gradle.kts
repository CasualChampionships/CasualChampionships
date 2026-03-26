plugins {
    val jvmVersion = libs.versions.fabric.kotlin.get()
        .split("+kotlin.")[1]
        .split("+")[0]

    kotlin("jvm").version(jvmVersion)
    kotlin("plugin.serialization").version(jvmVersion)
    alias(libs.plugins.fabric.loom)
    `maven-publish`
    java
}

allprojects {
    group = "net.casual"
    version = "2.3.0"

    apply(plugin = "net.fabricmc.fabric-loom")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    repositories {
        mavenLocal()
        maven("https://maven.supersanta.me/snapshots")
        maven("https://maven.maxhenkel.de/repository/public")
        maven("https://maven.parchmentmc.org/")
        maven("https://jitpack.io")
        maven("https://maven.nucleoid.xyz")
        maven("https://api.modrinth.com/maven")
        maven("https://maven.andante.dev/releases/")
        mavenCentral()
    }

    configurations.all {
        // This is to resolve any conflicts with arcade-datagen
        resolutionStrategy {
            force(rootProject.libs.arcade)
        }
    }

    dependencies {
        val libs = rootProject.libs

        minecraft(libs.minecraft)
        implementation(libs.fabric.loader)
        implementation(libs.fabric.api)
        implementation(libs.fabric.kotlin)

        implementation(libs.arcade)

        implementation(libs.map.canvas)
        implementation(libs.permissions)
    }

    java {
        withSourcesJar()
    }

    loom {
        runs {
            create("datagenClient") {
                client()
                programArgs("--arcade-datagen")
                runDir = "run-datagen"
            }
            named("server") {
                vmArgs("-Dmixin.debug.export=true")
            }
        }
    }

    tasks {
        processResources {
            inputs.property("version", version)
            filesMatching("fabric.mod.json") {
                expand(mutableMapOf(
                    "version" to version,
                    "minecraft_dependency" to libs.versions.minecraft.get(),
                    "fabric_api_dependency" to libs.versions.fabric.api.get(),
                    "fabric_kotlin_dependency" to libs.versions.fabric.kotlin.get(),
                ))
            }
        }

        jar {
            from("LICENSE")
        }
    }
}

subprojects {
    if (path != ":minigames-common") {
        dependencies {
            api(project(":minigames-common"))
        }
    }
}

dependencies {
    include(libs.arcade)
    include(libs.map.canvas)

    include(implementation(libs.voicechat.api.get())!!)
    include(implementation(libs.casual.database.get())!!)

    for (subproject in project.subprojects) {
        include(implementation(subproject)!!)
    }
}