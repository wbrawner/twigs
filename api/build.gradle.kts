plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.5.20"
    `java-library`
}

val ktorVersion: String by rootProject.extra

dependencies {
    implementation(kotlin("stdlib"))
    api(project(":core"))
    implementation(project(":storage"))
    api("io.ktor:ktor-server-core:$ktorVersion")
    api("io.ktor:ktor-serialization:$ktorVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}