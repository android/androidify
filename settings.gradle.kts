pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
            content {
                // This is required to use com.google.android.wearable.watchface.validator
                includeGroup("com.github.xgouchet")
            }
        }
    }
}

plugins {
    val agpVersion = File("gradle/libs.versions.toml").readLines().first {
        it.startsWith("agp =")
    }.split("\"")[1]
    id("com.android.settings") version agpVersion
}

android {
    compileSdk {
        version = release(36)
    }
    targetSdk {
        version = release(36)
    }
    minSdk {
        version = release(26)
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "Androidify"

includeBuild("build-plugin")

include(":app")
include(":feature:camera")
include(":feature:creation")
include(":feature:home")
include(":feature:results")
include(":data")
include(":core:network")
include(":core:util")
include(":core:theme")
include(":core:testing")
include(":core:xr")
include(":benchmark")
include(":watchface")
include(":wear")
include(":wear:watchface")
include(":wear:common")
