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
package com.android.developers.androidify.benchmark

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.benchmark.macro.ArtMetric
import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MemoryUsageMetric
import androidx.benchmark.macro.PowerMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.uiAutomator
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@RequiresApi(Build.VERSION_CODES.Q)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupNoPrecompilation() = startup(CompilationMode.None())

    @Test
    fun startUpProfileBaselineProfileUseIfAvailable() = startup(
        CompilationMode.Partial(BaselineProfileMode.UseIfAvailable, warmupIterations = 3)
    )

    @Test
    fun startUpProfileBaselineProfileRequired() = startup(
        CompilationMode.Partial(BaselineProfileMode.Require, warmupIterations = 3)
    )

    @Test
    fun startProfileBaselineProfile() = startup(
        CompilationMode.Partial(BaselineProfileMode.Disable, warmupIterations = 3)
    )

    @OptIn(ExperimentalMetricApi::class)
    private fun startup(compilationMode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = "com.android.developers.androidify",
        metrics = listOf(
            StartupTimingMetric(),
            ArtMetric(),
        ),
        iterations = 5,
        compilationMode = compilationMode,
        startupMode = StartupMode.COLD,
        setupBlock = {
            killProcess()
        }
    ) {
        uiAutomator {
            startApp(packageName = packageName)
        }
    }
}
