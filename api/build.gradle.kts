plugins {
    kotlin("jvm")
    `java-library`
}

dependencies {
    implementation(kotlin("stdlib"))
    api(project(":core"))
    implementation(project(":service"))
    implementation(project(":storage"))
    implementation(libs.ktor.server.routing.openapi)
    api(libs.ktor.server.core)
    api(libs.kotlinx.coroutines.core)
    testImplementation(project(":testhelpers"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}