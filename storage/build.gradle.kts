plugins {
    kotlin("jvm")
    `java-library`
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":core"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("org.postgresql:postgresql:42.2.23")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}