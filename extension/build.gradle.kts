plugins {
    `java-library`
}

val creekPlatformVersion : String by extra

dependencies {
    api("org.creekservice:creek-platform-metadata:$creekPlatformVersion")
}
