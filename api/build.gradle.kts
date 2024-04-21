plugins {
    kotlin("jvm")
    `java-library`
}

dependencies {
    implementation(kotlin("stdlib"))
    api(project(":core"))
    implementation(project(":service"))
    implementation(project(":storage"))
    api(libs.ktor.server.core)
    api(libs.kotlinx.coroutines.core)
    testImplementation(project(":testhelpers"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}