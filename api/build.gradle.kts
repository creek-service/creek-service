plugins {
    `java-library`
}

val creekBaseVersion : String by extra
val spotBugsVersion : String by extra

dependencies {
    api(project(":extension"))
    api("org.creekservice:creek-base-annotation:$creekBaseVersion")

    implementation("org.creekservice:creek-base-type:$creekBaseVersion")
    implementation("com.github.spotbugs:spotbugs-annotations:$spotBugsVersion")
}
