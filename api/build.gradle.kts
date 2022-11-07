plugins {
    `java-library`
}

val creekVersion : String by extra
val spotBugsVersion : String by extra

dependencies {
    api(project(":extension"))
    api("org.creekservice:creek-base-annotation:$creekVersion")

    implementation("org.creekservice:creek-base-type:$creekVersion")
    implementation("com.github.spotbugs:spotbugs-annotations:$spotBugsVersion")
}
