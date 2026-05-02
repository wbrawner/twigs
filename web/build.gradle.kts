plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":core"))
    implementation(project(":service"))
    api(libs.ktor.server.core)
    api(libs.ktor.server.mustache)
    implementation(libs.ktor.server.routing.openapi)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}