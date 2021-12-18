import java.net.URI
import java.util.*

plugins {
    java
    kotlin("jvm")
    application
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = URI("https://repo.maven.apache.org/maven2")
    }
}

val kotlinVersion: String by rootProject.extra
val ktorVersion: String by rootProject.extra

dependencies {
    implementation(project(":api"))
    implementation(project(":core"))
    implementation(project(":db"))
    implementation(project(":web"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-sessions:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("ch.qos.logback:logback-classic:1.2.8")
    testImplementation(project(":testhelpers"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
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
