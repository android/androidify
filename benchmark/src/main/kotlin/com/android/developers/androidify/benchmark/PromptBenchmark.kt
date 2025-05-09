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
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MemoryUsageMetric
import androidx.benchmark.macro.PowerMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import com.android.developers.androidify.classInitMetric
import com.android.developers.androidify.jitCompilationMetric
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@RequiresApi(Build.VERSION_CODES.Q)
class PromptBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun promptBenchmarkNoPrecompilation() = promptBenchmark(CompilationMode.None())

    @Test
    fun promptBenchmarkBaselineProfile() = promptBenchmark(CompilationMode.DEFAULT)

    @Test
    fun promptBenchmarkFullCompilation() = promptBenchmark(CompilationMode.Full())

    @OptIn(ExperimentalMetricApi::class)
    private fun promptBenchmark(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = "com.android.developers.androidify",
            compilationMode = compilationMode,
            metrics = listOf(
                StartupTimingMetric(),
                FrameTimingMetric(),
                MemoryUsageMetric(MemoryUsageMetric.Mode.Max),
                PowerMetric(PowerMetric.Type.Power()),
                jitCompilationMetric,
                classInitMetric,
            ),
            startupMode = StartupMode.COLD,
            iterations = 5,
        ) {
            uiAutomator {
                startApp(packageName = packageName)
                onView { textAsString() == "Let's Go" }.click()
                onView { textAsString() == "Prompt" }.click()
                onView { isEditable }.apply {
                    click()
                    text =
                        "wearing brown sneakers, a red t-shirt, " +
                        "big sunglasses and sports a purple mohawk."
                }
            }
        }
    }
}
