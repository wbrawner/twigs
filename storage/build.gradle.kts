plugins {
    kotlin("jvm")
    `java-library`
}

dependencies {
    implementation(kotlin("stdlib"))
    api(project(":core"))
    api(libs.kotlin.coroutines.core)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}