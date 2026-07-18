rootProject.name = "SnackeAI"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {

    repositories {
        gradlePluginPortal()
        maven("https://packages.jetbrains.team/maven/p/cmp/dev")
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://packages.jetbrains.team/maven/p/cmp/dev")
    }

    versionCatalogs {
        create("libs") {
            version("kotlin", extra["kotlin.version"].toString())
            version("compose", extra["compose.version"].toString())
            version("agp", extra["agp.version"].toString())
        }
        create("ui") {
            from(files("/gradle/ui.versions.toml"))
            version("kotlin", extra["kotlin.version"].toString())
            version("compose", extra["compose.version"].toString())
            version("agp", extra["agp.version"].toString())
        }
    }
}

include(":frontend:composeApp")
include(":contract:snakeai")
include(":backend")
