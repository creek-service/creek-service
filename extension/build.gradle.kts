plugins {
    `java-library`
}

val creekVersion : String by extra

dependencies {
    api("org.creekservice:creek-platform-metadata:$creekVersion")
}
