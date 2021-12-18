plugins {
    kotlin("jvm")
    java
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":storage"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.2")
    api("org.junit.jupiter:junit-jupiter-api:5.8.2")
    api("org.junit.jupiter:junit-jupiter-engine")
}
