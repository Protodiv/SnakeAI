import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(ui.plugins.kotlinMultiplatform)
//    alias(ui.plugins.androidApplication)
    alias(ui.plugins.composeMultiplatform)
    alias(ui.plugins.composeCompiler)
    alias(ui.plugins.composeHotReload)
    alias(libs.plugins.jetbrains.serialization)
}

kotlin {
//    androidTarget {
//        compilerOptions {
//            jvmTarget.set(JvmTarget.JVM_11)
//        }
//    }
    
    jvm()
    
//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs {
//        browser()
//        binaries.executable()
//    }
    
    sourceSets {
//        androidMain.dependencies {
//            implementation(compose.preview)
//            implementation(ui.androidx.activity.compose)
//        }
        commonMain.dependencies {

            implementation(ui.bundles.koin)
            implementation(libs.kotlinx.serialization.json)
            implementation(ui.precompose)
            implementation(ui.bundles.material)
            implementation(libs.bundles.ktor.common)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(ui.androidx.lifecycle.viewmodelCompose)
            implementation(ui.androidx.lifecycle.runtimeCompose)
            implementation(ui.kotlinx.collections.immutable)
        }
//        commonTest.dependencies {
//            implementation(ui.kotlin.test)
//        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(ui.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.cio)
        }
//        wasmJsMain.dependencies {
//        }
    }
}

//android {
//    namespace = "ua.protodiv.app"
//    compileSdk = ui.versions.android.compileSdk.get().toInt()
//
//    defaultConfig {
//        applicationId = "ua.protodiv.app"
//        minSdk = ui.versions.android.minSdk.get().toInt()
//        targetSdk = ui.versions.android.targetSdk.get().toInt()
//        versionCode = 1
//        versionName = "1.0"
//    }
//    packaging {
//        resources {
//            excludes += "/META-INF/{AL2.0,LGPL2.1}"
//        }
//    }
//    buildTypes {
//        getByName("release") {
//            isMinifyEnabled = false
//        }
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_11
//        targetCompatibility = JavaVersion.VERSION_11
//    }
//}

//dependencies {
//    debugImplementation(compose.uiTooling)
//}

compose.desktop {
    application {
        mainClass = "ua.protodiv.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ua.protodiv.app"
            packageVersion = "1.0.0"
        }
    }
}
