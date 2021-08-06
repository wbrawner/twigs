import java.net.URI

plugins {
    java
    kotlin("jvm")
    id("org.springframework.boot")
    application
}

apply(plugin = "io.spring.dependency-management")

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
    implementation(project(":storage"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-sessions:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.session:spring-session-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    runtimeOnly("mysql:mysql-connector-java:8.0.15")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test:5.1.5.RELEASE")
}

description = "twigs-server"

val twigsMain = "com.wbrawner.twigs.server.TwigsServerApplication"

application {
    mainClass.set(twigsMain)
}

tasks.bootJar {
    mainClassName = twigsMain
}

tasks.bootRun {
    mainClass.set(twigsMain)
}
