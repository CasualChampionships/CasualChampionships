base.archivesName.set("casual-uhc")
version = rootProject.version

dependencies {
    implementation(project(mapOf("path" to ":minigames:common", "configuration" to "namedElements")))
}
