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
