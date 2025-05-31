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
        maven {
            // You can find the maven URL for other artifacts (e.g. KMP, METALAVA) on their
            // build pages.
            url = uri("https://androidx.dev/snapshots/builds/13511472/artifacts/repository")
        }

    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://androidx.dev/snapshots/builds/13511472/artifacts/repository")
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "Androidify"
include(":app")

include(":feature")
include(":feature:camera")
include(":feature:creation")
include(":feature:home")
include(":feature:results")
include(":data")
include(":core")
include(":core:network")
include(":core:util")
include(":core:theme")
include(":core:testing")
include(":benchmark")
