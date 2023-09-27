import java.net.URI

plugins {
    java
    kotlin("jvm")
    application
    alias(libs.plugins.shadow)
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = URI("https://repo.maven.apache.org/maven2")
    }
}

dependencies {
    implementation(project(":api"))
    implementation(project(":core"))
    implementation(project(":db"))
    implementation(project(":web"))
    implementation(libs.kotlin.reflect)
    implementation(libs.bundles.ktor.server)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.logback)
    implementation(libs.mail)
    testImplementation(project(":testhelpers"))
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.ktor.server.test)
}

description = "twigs-server"

val twigsMain = "com.wbrawner.twigs.server.ApplicationKt"

application {
    mainClass.set(twigsMain)
}

tasks.shadowJar {
    manifest {
        attributes("Main-Class" to twigsMain)
        archiveBaseName.set("twigs")
        archiveClassifier.set("")
        archiveVersion.set("")
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.test {
    reports {
        junitXml.required.set(true)
        html.required.set(false)
    }
}
