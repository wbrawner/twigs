import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
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

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            url = URI("http://repo.maven.apache.org/maven2")
        }
    }
    group = "com.wbrawner"
    version = "0.0.1-SNAPSHOT"

    sourceSets.getByName("main") {
        java.srcDir("src/main/kotlin")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "14"
    }
}
