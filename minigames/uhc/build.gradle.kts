import org.apache.commons.io.output.ByteArrayOutputStream
import java.nio.charset.Charset

plugins {
    kotlin("jvm")
    id("fabric-loom")
    `maven-publish`
    java
}

group = property("maven_group")!!
version = this.getGitHash().substring(0, 6)

repositories {
    mavenLocal()
    maven {
        url = uri("https://maven.parchmentmc.org/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://maven.nucleoid.xyz")
    }
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${property("parchment_version")}@zip")
    })

    modImplementation("com.github.CasualChampionships:arcade:${property("arcade_version")}")
    modImplementation("xyz.nucleoid:fantasy:${property("fantasy_version")}")
    modImplementation("com.github.senseiwells:ServerReplay:${property("server_replay_version")}")
    modImplementation("eu.pb4:polymer-resource-pack:${property("polymer_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    implementation(project(mapOf("path" to ":minigames:common", "configuration" to "namedElements")))
}

tasks {
    sourceSets {
        create("datagen") {
            compileClasspath += main.get().compileClasspath
            runtimeClasspath += main.get().runtimeClasspath
            compileClasspath += main.get().output
            compileClasspath += test.get().compileClasspath
            runtimeClasspath += test.get().runtimeClasspath
        }
    }

    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }

    jar {
        from("LICENSE")
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifact(remapJar) {
                    builtBy(remapJar)
                }
                artifact(kotlinSourcesJar) {
                    builtBy(remapSourcesJar)
                }
            }
        }

        repositories {

        }
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}

java {
    withSourcesJar()
}

fun getGitHash(): String {
    val out = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "HEAD")
        standardOutput = out
    }
    return out.toString(Charset.defaultCharset()).trim()
}