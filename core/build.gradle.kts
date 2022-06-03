plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    api(libs.ktor.auth)
    api(libs.bcrypt)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}