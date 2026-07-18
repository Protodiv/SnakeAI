import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(ui.plugins.kotlinMultiplatform)
    alias(ui.plugins.androidMultiplatformLibrary)
    alias(ui.plugins.composeMultiplatform)
    alias(ui.plugins.composeCompiler)
}

kotlin {
    wasmJs {
        browser()
        binaries.executable()
    }

    jvm()

    android {
        namespace = "ua.snakeai.app"
        compileSdk = 37
        minSdk = 26

        androidResources {
            enable = true
        }
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("androidx.compose.material3.ExperimentalMaterial3Api")
            }
        }

        commonMain.dependencies {
            implementation(projects.contract.snakeai)

            implementation(ui.bundles.koin)
            implementation(libs.kotlinx.serialization.json)
            implementation(ui.bundles.material)
            implementation(libs.bundles.ktor.common)

            implementation(ui.kotlinx.collections.immutable)

            implementation(ui.compose.foundation)
            implementation(ui.compose.material3)
            implementation(ui.components.resources)
            implementation(ui.compose.ui.tooling.preview)
            implementation(ui.material.icons.core)

            implementation(ui.androidx.lifecycle.runtime.compose)
            implementation(ui.androidx.lifecycle.viewmodel.compose)
            implementation(ui.androidx.navigation.compose)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(ui.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.cio)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
            implementation(libs.kotlinx.serialization.json.wasm)
        }
    }
}

compose.desktop {
    application {
        mainClass = "ua.snakeai.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ua.snakeai.app"
            packageVersion = "1.0.0"
        }

        buildTypes.release.proguard {
            configurationFiles.from(project.file("rules.pro"))
        }
    }
}