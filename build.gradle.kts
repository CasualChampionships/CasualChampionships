plugins {
    id("casual.common-conventions")
}

arcade {
    modules("arcade")
}

dependencies {
    include(libs.map.canvas)
    include(libs.polymer.core)

    include(implementation(libs.voicechat.api.get())!!)
    include(implementation(libs.casual.database.get())!!)

    for (subproject in subprojects) {
        include(implementation(subproject)!!)
    }
}