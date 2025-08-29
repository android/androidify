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
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "Androidify"
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
