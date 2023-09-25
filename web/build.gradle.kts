import java.util.*

plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(kotlin("stdlib"))
    api(libs.ktor.server.core)
    implementation(libs.ktor.server.html)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

// TODO: Replace this hack with either a git submodule or an internal Kotlin-based UI
tasks.register("package") {
    doLast {
        val built = File(rootProject.rootDir.parent, "twigs-web/dist/twigs")
        if (built.exists()) {
            built.deleteRecursively()
        }
        val dest = File(project.projectDir, "src/main/resources/twigs")
        if (dest.exists()) {
            dest.deleteRecursively()
        }
        var command = listOf(
            "cd", "../../twigs-web", ";",
            "npm", "i", ";",
            "npm", "run", "package"
        )
        command = if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")) {
            listOf("powershell", "-Command") + command
        } else {
            listOf("bash", "-c", "\"${command.joinToString(" ")}\"")
        }
        exec {
            commandLine(command)
        }
        if (!built.copyRecursively(dest, true) || !dest.isDirectory) {
            throw GradleException("Failed to copy files from ${built.absolutePath} to ${dest.absolutePath}")
        }
    }
}

//tasks.getByName("processResources") {
//    dependsOn.add("package")
//}
