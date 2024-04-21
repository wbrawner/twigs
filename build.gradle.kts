import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        classpath(libs.kotlin.gradle)
    }
}

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization) apply false
}

val javaVersion = JavaVersion.VERSION_17

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
    tasks.withType<JavaCompile> {
        sourceCompatibility = javaVersion.majorVersion
        targetCompatibility = javaVersion.majorVersion
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = javaVersion.majorVersion
    }
}
