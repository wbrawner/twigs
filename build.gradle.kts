import java.net.URI
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    val kotlinVersion: String by extra("1.5.20")
    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = java.net.URI("https://repo.spring.io/snapshot") }
        maven { url = java.net.URI("https://repo.spring.io/milestone") }
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.springframework.boot:spring-boot-gradle-plugin:2.2.4.RELEASE")
    }
}

plugins {
    java
    kotlin("jvm") version "1.5.10"
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            url = URI("https://repo.maven.apache.org/maven2")
        }
    }
    group = "com.wbrawner"
    version = "0.0.1-SNAPSHOT"
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "14"
    }
}
