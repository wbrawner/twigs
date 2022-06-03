plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
    `java-library`
}

dependencies {
    implementation(kotlin("stdlib"))
    api(project(":core"))
    implementation(project(":storage"))
    api(libs.ktor.server.core)
    api(libs.ktor.serialization)
    api(libs.kotlin.coroutines.core)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}