plugins {
    kotlin("jvm")
    java
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":storage"))
    api(libs.kotlinx.coroutines.test)
    api(libs.junit.jupiter.api)
    implementation(project(mapOf("path" to ":db")))
    runtimeOnly(libs.junit.jupiter.engine)
}

tasks {
    test {
        useJUnitPlatform()
    }
}