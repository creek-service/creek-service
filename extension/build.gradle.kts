plugins {
    `java-library`
}

val creekMetadataVersion : String by extra

dependencies {
    api("org.creekservice:creek-platform-metadata:$creekMetadataVersion")
}
