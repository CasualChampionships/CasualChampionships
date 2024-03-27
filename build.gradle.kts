import org.apache.commons.io.output.ByteArrayOutputStream
import java.nio.charset.Charset

plugins {
    kotlin("jvm")
    id("fabric-loom")
    `maven-publish`
    java
}

val modVersion: String by project
group = "net.casual"
version = this.getGitHash().substring(0, 6)

val minecraftVersion: String by project
val parchmentVersion: String by project
val loaderVersion: String by project
val fabricVersion: String by project
val fabricKotlinVersion: String by project

val arcadeVersion: String by project
val arcadeDatagenVersion: String by project
val serverReplayVersion: String by project
val serverTranslationsVersion: String by project

allprojects {
    apply(plugin = "fabric-loom")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenLocal()
        maven("https://maven.parchmentmc.org/")
        maven("https://jitpack.io")
        maven("https://maven.nucleoid.xyz")
        maven("https://repo.fruxz.dev/releases/")
        mavenCentral()
    }

    configurations.all {
        // This is to resolve any conflicts with arcade-datagen
        resolutionStrategy {
            force("com.github.CasualChampionships:arcade:$arcadeVersion")
        }
    }

    dependencies {
        minecraft("com.mojang:minecraft:$minecraftVersion")
        @Suppress("UnstableApiUsage")
        mappings(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-$parchmentVersion@zip")
        })
        modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
        modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
        modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

        modImplementation("com.github.CasualChampionships:arcade:$arcadeVersion")
        modImplementation("com.github.CasualChampionships:arcade-datagen:$arcadeDatagenVersion")
        modImplementation("com.github.senseiwells:ServerReplay:$serverReplayVersion")
        modImplementation("xyz.nucleoid:server-translations-api:$serverTranslationsVersion")
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
        }
    }

    tasks {
        processResources {
            inputs.property("version", version)
            filesMatching("fabric.mod.json") {
                expand(mutableMapOf("version" to version))
            }
        }

        jar {
            from("LICENSE")
        }

        compileKotlin {
            kotlinOptions.jvmTarget = "17"
        }
    }
}

dependencies {
    include("com.github.CasualChampionships:arcade:$arcadeVersion")
    include("com.github.senseiwells:ServerReplay:$serverReplayVersion")
    include("xyz.nucleoid:server-translations-api:$serverTranslationsVersion")

    include(implementation(project(mapOf("path" to ":minigames:common", "configuration" to "namedElements")))!!)
    include(implementation(project(mapOf("path" to ":minigames:uhc", "configuration" to "namedElements")))!!)
    include(implementation(project(mapOf("path" to ":minigames:duel", "configuration" to "namedElements")))!!)

    include(implementation("org.mongodb:mongo-java-driver:3.12.11")!!)
}

fun getGitHash(): String {
    val out = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "HEAD")
        standardOutput = out
    }
    return out.toString(Charset.defaultCharset()).trim()
}