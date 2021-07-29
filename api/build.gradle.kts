plugins {
    kotlin("jvm")
    `java-library`
}

val ktorVersion: String by rootProject.extra

dependencies {
    implementation(kotlin("stdlib"))
    api(project(":core"))
    implementation(project(":storage"))
    api("io.ktor:ktor-server-core:$ktorVersion")
    api("io.ktor:ktor-auth:$ktorVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}