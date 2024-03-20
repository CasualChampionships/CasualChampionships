base.archivesName.set("casual-common")
version = rootProject.version

sourceSets {
    val datagen by creating {
        compileClasspath += main.get().compileClasspath
        runtimeClasspath += main.get().runtimeClasspath
        compileClasspath += main.get().output
    }
}

loom {
    createRemapConfigurations(sourceSets["datagen"])

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

dependencies {
    "modDatagenImplementation"("com.github.CasualChampionships:arcade-datagen:1.0.4")
}