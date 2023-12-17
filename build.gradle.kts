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
        url = uri("https://masa.dy.fi/maven")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://maven.nucleoid.xyz")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${property("parchment_version")}@zip")
    })

    include(modImplementation("com.github.CasualChampionships:arcade:${property("arcade_version")}")!!)

    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    include(modImplementation("xyz.nucleoid:server-translations-api:${property("server_translations_api_version")}")!!)
    include(modImplementation("xyz.nucleoid:fantasy:${property("fantasy_version")}")!!)
    include(modImplementation("com.github.senseiwells:ServerReplay:${property("server_replay_version")}")!!)
    include(modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")!!)
    modImplementation("me.lucko:fabric-permissions-api:${property("permissions_version")}")

    modImplementation("eu.pb4:polymer-core:${property("polymer_version")}")
    modImplementation("eu.pb4:polymer-blocks:${property("polymer_version")}")
    modImplementation("eu.pb4:polymer-resource-pack:${property("polymer_version")}")
    modImplementation("eu.pb4:polymer-virtual-entity:${property("polymer_version")}")

    include(modImplementation("com.github.ReplayMod:ReplayStudio:6cd39b0874") {
        exclude(group = "org.slf4j")
        exclude(group = "com.google.guava", module = "guava-jdk5")
        exclude(group = "com.google.guava", module = "guava")
        exclude(group = "com.google.code.gson", module = "gson")
    })

    include(implementation("org.mongodb:mongo-java-driver:3.12.11")!!)
    // include(implementation("org.java-websocket:Java-WebSocket:1.5.3")!!)

    implementation(kotlin("stdlib-jdk8"))

    testImplementation("org.apache.commons:commons-text:1.11.0")
}

tasks {
    sourceSets {
        create("testmod") {
            compileClasspath += main.get().compileClasspath
            runtimeClasspath += main.get().runtimeClasspath
            compileClasspath += main.get().output
            compileClasspath += test.get().compileClasspath
            runtimeClasspath += test.get().runtimeClasspath
        }
    }

    loom {
        mods {
            create("testmod") {
                sourceSet(sourceSets["testmod"])
            }
        }

        runs {
            create("testmodClient") {
                client()
                name = "Test Mod Client"
                runDir = "run-test"
                setSource(sourceSets["testmod"])
            }
        }

        accessWidenerPath.set(file("src/main/resources/uhc.accesswidener"))
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