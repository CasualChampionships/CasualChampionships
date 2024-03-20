base.archivesName.set("casual-duel")
version = rootProject.version

dependencies {
    implementation(project(mapOf("path" to ":minigames:common", "configuration" to "namedElements")))
}
