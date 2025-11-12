package com.android.developers.androidify.plugin

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

private val Project.versionCatalog: VersionCatalog
    get() = project.extensions.getByType<VersionCatalogsExtension>().find("libs").get()

internal fun Project.getVersionByName(name: String): String {
    val version = versionCatalog.findVersion(name)
    return if (version.isPresent) {
        version.get().requiredVersion
    } else {
        throw GradleException("Could not find a version for `$name`")
    }
}
