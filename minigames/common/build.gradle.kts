plugins {
    kotlin("jvm")
    id("fabric-loom")
    `maven-publish`
    java
}

group = property("maven_group")!!
version = property("mod_version")!!

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

sourceSets {
    val datagen by creating {
        compileClasspath += main.get().compileClasspath
        runtimeClasspath += main.get().runtimeClasspath
        compileClasspath += main.get().output
    }
}

loom {
    createRemapConfigurations(sourceSets["datagen"])
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${property("parchment_version")}@zip")
    })

    include(modApi("com.github.CasualChampionships:arcade:${property("arcade_version")}")!!)
    include(modApi("com.github.senseiwells:ServerReplay:${property("server_replay_version")}")!!)
    include(modImplementation("xyz.nucleoid:server-translations-api:${property("server_translations_api_version")}")!!)

    modApi("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
    modApi("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    "modDatagenImplementation"("com.github.CasualChampionships:arcade-datagen:1.0.3")
    "modDatagenImplementation"("org.apache.commons:commons-text:1.11.0")
}

loom {
    mods {
        create("datagen") {
            sourceSet(sourceSets["datagen"])
        }
    }

    runs {
        create("datagenClient") {
            client()
            name = "Test Mod Client"
            runDir = "run-datagen"
            setSource(sourceSets["datagen"])
        }
    }
}

tasks {
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
