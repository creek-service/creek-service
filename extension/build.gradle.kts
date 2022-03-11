plugins {
    `java-library`
}

val creekVersion : String by extra

dependencies {
    api("org.creek:creek-platform-metadata:$creekVersion")
}
