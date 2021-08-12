import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

buildscript {
    val kotlinVersion: String by extra("1.5.20")
    val ktorVersion: String by extra("1.6.1")
    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

plugins {
    java
    kotlin("jvm") version "1.5.20"
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
        kotlinOptions.jvmTarget = "16"
    }
}
