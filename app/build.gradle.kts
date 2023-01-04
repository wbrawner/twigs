import java.net.URI

plugins {
    java
    kotlin("jvm")
    application
    alias(libs.plugins.shadow)
    alias(libs.plugins.graalvm)
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
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
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

graalvmNative {
    binaries {
        named("main") {
            fallback.set(false)
            verbose.set(true)

            buildArgs.add("--initialize-at-build-time=io.ktor,kotlin,kotlinx.serialization,ch.qos.logback,org.slf4j")
            buildArgs.add("--trace-object-instantiation=ch.qos.logback.classic.Logger")
            buildArgs.add("-H:+InstallExitHandlers")
            buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
            buildArgs.add("-H:+ReportExceptionStackTraces")

            imageName.set("twigs")
        }
    }
}
