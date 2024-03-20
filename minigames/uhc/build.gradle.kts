base.archivesName.set("casual-common")
version = rootProject.version

dependencies {
    implementation(project(mapOf("path" to ":minigames:common", "configuration" to "namedElements")))
}
