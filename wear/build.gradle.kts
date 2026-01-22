/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.ByteArrayOutputStream
import java.util.regex.Pattern
import org.gradle.api.attributes.Attribute
import com.android.build.api.attributes.BuildTypeAttr

plugins {
    alias(libs.plugins.androidify.androidApplication)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "com.android.developers.androidify"
    defaultConfig {
        minSdk = 36
        applicationId = "com.android.developers.androidify"
        // Ensure Wear OS app has its own version space
        versionCode = libs.versions.appVersionWearOffset.get().toInt() + libs.versions.appVersionCode.get().toInt()
        versionName = libs.versions.appVersionName.get()
    }
}

configurations {
    create("cliToolConfiguration") {
        isCanBeConsumed = false
        isCanBeResolved = true
    }

    listOf("debug", "release").forEach { buildType ->
        create("watchfaceApk${buildType.replaceFirstChar { it.uppercase() }}") {
            isCanBeResolved = true
            isCanBeConsumed = false
            isTransitive = false

            attributes {
                attribute(
                    Attribute.of(BuildTypeAttr::class.java),
                    objects.named(BuildTypeAttr::class.java, buildType)
                )
                attribute(Attribute.of("artifactType", String::class.java), "apk")
            }

            dependencies.add(project.dependencies.create(project(":wear:watchface")))
        }
    }
}

dependencies {
    implementation(projects.wear.common)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.wear.compose.ui.tooling)
    implementation(libs.androidx.activity.compose)
    implementation(libs.play.services.wearable)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.watchface.push)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.androidx.wear)
    implementation(libs.androidx.wear.remote.interactions)
    implementation(libs.horologist.compose.layout)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.work.runtime.ktx)

    "cliToolConfiguration"(libs.validator.push.cli)
}

androidComponents.onVariants { variant ->
    val capsVariant = variant.name.replaceFirstChar { it.uppercase() }
    val tokenTask = tasks.register<ProcessFilesTask>("generateToken${capsVariant}Res") {
        apkFileCollection.from(configurations.getByName("watchfaceApk$capsVariant"))
        cliToolClasspath.from(project.configurations["cliToolConfiguration"])
    }
    variant.sources.assets!!.addGeneratedSourceDirectory(tokenTask) { task -> task.apkDirectory }
    variant.sources.res!!.addGeneratedSourceDirectory(tokenTask) { task -> task.resDirectory }
}

abstract class ProcessFilesTask : DefaultTask() {
    @get:[InputFiles Classpath]
    abstract val cliToolClasspath: ConfigurableFileCollection
    @get:[InputFiles PathSensitive(PathSensitivity.NONE)]
    abstract val apkFileCollection: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val apkDirectory: DirectoryProperty
    @get:OutputDirectory
    abstract val resDirectory: DirectoryProperty

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun taskAction() {
        val sourceApkFile = apkFileCollection.files.single().listFiles().single { it.extension == "apk" }
        val destinationApkFile = File(apkDirectory.asFile.get(), "default_watchface.apk")
        sourceApkFile.copyTo(destinationApkFile, overwrite = true)

        val stdOut = ByteArrayOutputStream()
        val stdErr = ByteArrayOutputStream()

        execOperations.javaexec {
            classpath = cliToolClasspath
            mainClass = "com.google.android.wearable.watchface.validator.cli.DwfValidation"

            args(
                "--apk_path=${destinationApkFile.absolutePath}",
                "--package_name=com.android.developers.androidify",
            )
            standardOutput = stdOut
            errorOutput = stdErr
            isIgnoreExitValue = true
        }

        val outputAsText = stdOut.toString()
        val errorAsText = stdErr.toString()

        if (outputAsText.contains("Failed check")) {
            println(outputAsText)
            if (errorAsText.isNotEmpty()) {
                println(errorAsText)
            }
            throw GradleException("Watch face validation failed")
        }

        val match = Pattern.compile("generated token: (\\S+)").matcher(stdOut.toString())
        if (match.find()) {
            val token = match.group(1)
            val output = File(resDirectory.get().asFile, "values/wf_token.xml")
            output.parentFile.mkdirs()
            val tokenResText = """<resources>
                         |    <string name="default_wf_token">$token</string>
                         |</resources>
                       """.trimMargin()
            output.writeText(tokenResText)
        } else {
            throw TaskExecutionException(
                this,
                GradleException("No token generated for watch face!"),
            )
        }
    }
}
