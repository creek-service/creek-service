plugins {
    `java-library`
}

val creekVersion : String by extra

dependencies {
    api(project(":extension"))
    api("org.creek:creek-base-annotation:$creekVersion")

    implementation("org.creek:creek-observability-logging:$creekVersion")
    implementation("org.creek:creek-base-type:$creekVersion")
}
