import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22" apply false
    id("org.springframework.boot") version "2.7.8" apply false
    id("io.spring.dependency-management") version "1.1.0"
}

repositories {
    mavenCentral()
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
    }

    group = "com.example"
    version = "0.0.1-SNAPSHOT"
    java.targetCompatibility = JavaVersion.VERSION_17
    java.sourceCompatibility = JavaVersion.VERSION_17

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation(platform("io.opentelemetry:opentelemetry-bom:1.23.1"))

        implementation("org.jetbrains.kotlin:kotlin-stdlib")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
