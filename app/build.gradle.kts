import java.net.URI
import java.util.*

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
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.sessions)
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

val captainDefinition = File(project.buildDir, "captain-definition")
val tarFile = File(project.buildDir, "twigs.tar")

tasks.register("package") {
    dependsOn(":app:shadowJar")
    doLast {
        captainDefinition.createNewFile()
        captainDefinition.outputStream().writer().use {
            it.appendLine(
                """
            {
                "schemaVersion": 2,
                "dockerfileLines": [
                    "FROM adoptopenjdk:openj9",
                    "COPY libs/twigs.jar twigs.jar",
                    "CMD /opt/java/openjdk/bin/java ${'$'}JVM_ARGS -jar /twigs.jar"
                ]
            }
        """.trimIndent()
            )
        }
        exec {
            commandLine(
                "tar",
                "-C",
                project.buildDir.absolutePath,
                "-cf",
                project.buildDir.name + File.separator + tarFile.name,
                captainDefinition.name,
                "libs/twigs.jar"
            )
        }
    }
}

tasks.register("publish") {
    dependsOn(":app:package")
    doLast {
        var command = listOf("caprover", "deploy", "-t", "build/${tarFile.name}", "-n", "wbrawner", "-a", "twigs-dev")
        command = if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")) {
            listOf("powershell", "-Command") + command
        } else {
            listOf("bash", "-c", "\"${command.joinToString(" ")}\"")
        }
        exec {
            commandLine(command)
        }
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
