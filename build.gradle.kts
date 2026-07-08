plugins {
    alias(libs.plugins.kotlin.jvm) apply false

    alias(ui.plugins.composeHotReload) apply false
    alias(ui.plugins.composeMultiplatform) apply false
    alias(ui.plugins.composeCompiler) apply false
    alias(ui.plugins.kotlinMultiplatform) apply false
}

group = "ua.protodiv"
version = "1.0-SNAPSHOT"