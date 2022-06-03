plugins {
    kotlin("jvm")
    java
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":storage"))
    api(libs.kotlin.coroutines.test)
    api(libs.junit.jupiter.api)
    runtimeOnly(libs.junit.jupiter.engine)
}
