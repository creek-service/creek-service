plugins {
    `java-library`
}

val creekVersion : String by extra
val spotBugsVersion : String by extra

dependencies {
    api(project(":api"))
    api("org.creekservice:creek-base-annotation:$creekVersion")
    api("org.creekservice:creek-base-type:$creekVersion")

    implementation("org.creekservice:creek-observability-logging:$creekVersion")
    implementation("org.creekservice:creek-platform-resource:$creekVersion")
    implementation("com.github.spotbugs:spotbugs-annotations:$spotBugsVersion")
}
