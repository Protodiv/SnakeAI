plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    war
    alias(libs.plugins.jetbrains.serialization) apply false
    alias(libs.plugins.buildkonfig) apply false

    alias(ui.plugins.androidApplication) apply false
    alias(ui.plugins.androidMultiplatformLibrary) apply false
    alias(ui.plugins.kotlinMultiplatform) apply false
    alias(ui.plugins.composeMultiplatform) apply false
    alias(ui.plugins.composeCompiler) apply false
    alias(ui.plugins.composeHotReload) apply false
}

group = "ua.snakeai"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get().toInt())
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}