import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
//        maven {
//            url = URI("https://repo.maven.apache.org/maven2")
//        }
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61")
    }
}

plugins {
    java
    kotlin("jvm") version "1.3.61"
    id("org.springframework.boot") version "2.2.4.RELEASE"
}

apply(plugin = "io.spring.dependency-management")

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = URI("http://repo.maven.apache.org/maven2")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.session:spring-session-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.61")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.61")
    implementation("io.springfox:springfox-swagger2:2.8.0")
    implementation("io.springfox:springfox-swagger-ui:2.8.0")
    runtimeOnly("mysql:mysql-connector-java:8.0.15")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test:5.1.5.RELEASE")
}

description = "twigs-server"

val mainClass = "com.wbrawner.budgetserver.BudgetServerApplication"
tasks.bootJar {
    mainClassName = mainClass
}

tasks.bootRun {
    main = mainClass
}
