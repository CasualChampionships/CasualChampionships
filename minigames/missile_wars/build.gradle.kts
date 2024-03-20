base.archivesName.set("casual-missile-wars")
version = rootProject.version

dependencies {
    implementation(project(mapOf("path" to ":minigames:common", "configuration" to "namedElements")))
}
