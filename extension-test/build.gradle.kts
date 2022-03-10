plugins {
    `java-library`
}

dependencies {
    implementation(project(":extension"))

    testRuntimeOnly(project(":test-java-eight-extension"))
    testRuntimeOnly(project(":test-java-nine-extension"))
}
