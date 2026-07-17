plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.jetbrains.serialization)
}

group = "ua.snakeai"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(projects.contract.snakeai)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.h2.database)
    implementation(libs.dl4j.core)
    implementation(libs.nd4j.native)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}
