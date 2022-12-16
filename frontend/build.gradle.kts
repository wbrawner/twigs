plugins {
    `java-library`
    alias(libs.plugins.node.gradle)
}

tasks.register<com.github.gradle.node.npm.task.NpmTask>("package") {
    group = "build"
    inputs.files(fileTree("node_modules"))
    inputs.files(fileTree("src"))
    inputs.file("package.json")
    inputs.file("swa-cli.config.json")
    inputs.file("tsconfig.json")
    inputs.file("vite.config.ts")

    outputs.dir("build/resources/main/static")

    dependsOn.add(tasks.getByName("npmInstall"))
    args.set(listOf("run", "build"))
}

tasks.getByName("processResources") {
    dependsOn.add("package")
}
