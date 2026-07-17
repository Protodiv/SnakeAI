plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.jetbrains.serialization) apply false

    alias(ui.plugins.composeHotReload) apply false
    alias(ui.plugins.composeMultiplatform) apply false
    alias(ui.plugins.composeCompiler) apply false
    alias(ui.plugins.kotlinMultiplatform) apply false
}

group = "ua.snakeai"
version = "1.0-SNAPSHOT"