import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("net.fabricmc.fabric-loom")
    id("net.casualchampionships.joystick")
    java
}

val libs = the<LibrariesForLibs>()

val modVersion: String = providers.gradleProperty("mod_version").get()

group = "net.casual"
version = modVersion

repositories {
    mavenLocal()
    maven("https://maven.casualchampionships.net/snapshots")
    maven("https://maven.supersanta.me/snapshots")
    maven("https://maven.maxhenkel.de/repository/public")
    maven("https://maven.parchmentmc.org/")
    maven("https://jitpack.io")
    maven("https://maven.nucleoid.xyz")
    maven("https://api.modrinth.com/maven")
    mavenCentral()
}

dependencies {
    minecraft(libs.minecraft)

    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)
    implementation(libs.fabric.kotlin)

    implementation(libs.map.canvas)
    implementation(libs.polymer.core)
}

arcade {
    version = libs.versions.arcade
    modules("arcade")
    include = false
}

java {
    withSourcesJar()
}

loom {
    runs {
        create("datagenClient") {
            client()
            programArguments.add("--arcade-datagen")
            runDirectory.set(file("run-datagen"))
        }
        named("server") {
            jvmArguments.add("-Dmixin.debug.export=true")
        }
    }
}

tasks {
    processResources {
        inputs.property("version", modVersion)
        filesMatching("fabric.mod.json") {
            expand(mapOf(
                "version" to modVersion,
                "minecraft_dependency" to "~${libs.versions.minecraft.get()}",
                "fabric_api_dependency" to libs.versions.fabric.api.get(),
                "fabric_kotlin_dependency" to libs.versions.fabric.kotlin.get(),
            ))
        }
    }

    jar {
        from(rootProject.file("LICENSE"))
    }
}
