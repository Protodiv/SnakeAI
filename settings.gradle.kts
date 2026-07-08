rootProject.name = "SnackeAI"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

//plugins {
//    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
//}

pluginManagement {

    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }

    versionCatalogs {
        create("ui") {
            from(files("/gradle/ui.versions.toml"))
        }
    }
}

include(":frontend:composeApp")