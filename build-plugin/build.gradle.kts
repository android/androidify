plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePluginApi)
    compileOnly(libs.kotlin.gradlePlugin)
}


tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register(libs.plugins.androidify.androidLibrary.get().pluginId) {
            id = libs.plugins.androidify.androidLibrary.get().pluginId
            implementationClass = "com.android.developers.androidify.plugin.AndroidLibraryPlugin"
        }
        register(libs.plugins.androidify.androidComposeLibrary.get().pluginId) {
            id = libs.plugins.androidify.androidComposeLibrary.get().pluginId
            implementationClass = "com.android.developers.androidify.plugin.AndroidComposeLibraryPlugin"
        }
        register(libs.plugins.androidify.androidApplication.get().pluginId) {
            id = libs.plugins.androidify.androidApplication.get().pluginId
            implementationClass = "com.android.developers.androidify.plugin.AndroidApplicationPlugin"
        }
    }
}
