plugins {
    kotlin("jvm")
    `java-library`
}

val ktorVersion: String by rootProject.extra

dependencies {
    implementation(kotlin("stdlib"))
    api(project(":storage"))
    implementation(libs.postgres)
    api(libs.hikari)
    implementation(libs.logback)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}