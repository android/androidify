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
import java.lang.RuntimeException

evaluationDependsOn(":wear:watchface")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "com.android.developers.androidify"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.wearMinSdk.get().toInt()
        applicationId = "com.android.developers.androidify"
        targetSdk = 36
        // Ensure Wear OS app has its own version space
        versionCode = libs.versions.appVersionWearOffset.get().toInt() + libs.versions.appVersionCode.get().toInt()
        versionName = libs.versions.appVersionName.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
    }
    kotlinOptions {
        jvmTarget = libs.versions.jvmTarget.get()
    }
    buildFeatures {
        compose = true
    }
    sourceSets {
        getByName("release") {
            assets.srcDirs(layout.buildDirectory.dir("intermediates/watchfaceAssets/release"))
            res.srcDirs(layout.buildDirectory.file("generated/wfTokenRes/release/res/"))
        }
        getByName("debug") {
            assets.srcDirs(layout.buildDirectory.dir("intermediates/watchfaceAssets/debug"))
            res.srcDirs(layout.buildDirectory.file("generated/wfTokenRes/debug/res/"))
        }
    }
}

configurations {
    create("cliToolConfiguration") {
        isCanBeConsumed = false
        isCanBeResolved = true
    }

    create("watchfaceApkDebug"){
        isCanBeResolved = true
        isCanBeConsumed = false

        attributes {
            attribute(
                Attribute.of(com.android.build.api.attributes.BuildTypeAttr::class.java),
                objects.named(com.android.build.api.attributes.BuildTypeAttr::class.java, "debug")
            )
            attribute(Attribute.of("artifactType", String::class.java), "apk")
        }
    }

    create("watchfaceApkRelease") {
        isCanBeResolved = true
        isCanBeConsumed = false

        attributes {
            attribute(
                Attribute.of(com.android.build.api.attributes.BuildTypeAttr::class.java),
                objects.named(com.android.build.api.attributes.BuildTypeAttr::class.java, "release")
            )
            attribute(Attribute.of("artifactType", String::class.java), "apk")
        }
    }
}

dependencies {
    configurations.getByName("watchfaceApkDebug").dependencies.add(project(":wear:watchface"))
    configurations.getByName("watchfaceApkRelease").dependencies.add(project(":wear:watchface"))

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

    val watchfaceApkConfig = when (variant.name) {
        "release" -> configurations.getByName("watchfaceApkRelease")
        "debug" -> configurations.getByName("watchfaceApkDebug")
        else -> throw RuntimeException("Cannot find watchface apk configuration")
    }

    val copyWatchfaceApkTask = tasks.register<Copy>("copyWatchface${capsVariant}ApkToAssets") {
        from(watchfaceApkConfig) {
            // the resolved directory contains apk and output-metadata.json
            include("*.apk")
        }
        into(layout.buildDirectory.dir("intermediates/watchfaceAssets/${variant.name}"))

        eachFile {
            path = "default_watchface.apk"
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        includeEmptyDirs = false
    }
    val tokenTask = tasks.register<ProcessFilesTask>("generateToken${capsVariant}Res") {
        val tokenFile =
            layout.buildDirectory.file("generated/wfTokenRes/${variant.name}/res/values/wf_token.xml")
        inputFile.from(copyWatchfaceApkTask.map { it.outputs.files.singleFile })
        outputFile.set(tokenFile)
        cliToolClasspath.set(project.configurations["cliToolConfiguration"])
    }

    afterEvaluate {
        tasks.named("pre${capsVariant}Build").configure {
            dependsOn(tokenTask)
        }
    }
}

abstract class ProcessFilesTask : DefaultTask() {
    @get:InputFiles
    abstract val inputFile: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val cliToolClasspath: Property<FileCollection>

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun taskAction() {
        val apkDirectory = inputFile.singleFile
        val apkFile = apkDirectory.resolve("default_watchface.apk")

        val stdOut = ByteArrayOutputStream()
        val stdErr = ByteArrayOutputStream()

        execOperations.javaexec {
            classpath = cliToolClasspath.get()
            mainClass = "com.google.android.wearable.watchface.validator.cli.DwfValidation"

            args(
                "--apk_path=${apkFile.absolutePath}",
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
            val output = outputFile.get().asFile
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
