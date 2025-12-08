package com.android.developers.androidify.plugin

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class AndroidComposeLibraryPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            apply(plugin = "androidify.androidLibrary")
            apply(plugin = "org.jetbrains.kotlin.plugin.compose")
            extensions.configure<LibraryExtension> {
                defaultConfig {
                    testInstrumentationRunner = "com.android.developers.testing.AndroidifyTestRunner"
                }
                buildFeatures.compose = true
            }
        }
    }
}
