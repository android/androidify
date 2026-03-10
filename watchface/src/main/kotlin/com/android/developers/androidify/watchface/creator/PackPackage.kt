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
package com.android.developers.androidify.watchface.creator

import android.util.Base64
import java.nio.charset.StandardCharsets

data class PackPackage(
    val androidManifest: String,
) {
    var resources: MutableList<Resource> = mutableListOf()

    data class Resource(
        @JvmField val subdirectory: String,
        @JvmField val name: String,
        @JvmField val contentsBase64: String,
    ) {
        companion object {
            fun fromBase64Contents(
                subdirectory: String,
                name: String,
                contentsBase64: String,
            ) = Resource(subdirectory, name, contentsBase64)

            fun fromByteArrayContents(
                subdirectory: String,
                name: String,
                contentsBytes: ByteArray,
            ): Resource {
                val encodedContents = Base64.encodeToString(contentsBytes, Base64.NO_WRAP)
                return fromBase64Contents(subdirectory, name, encodedContents)
            }

            fun fromStringContents(
                subdirectory: String,
                name: String,
                contentsString: String,
            ): Resource {
                val bytes = contentsString.toByteArray(StandardCharsets.UTF_8)
                return fromByteArrayContents(subdirectory, name, bytes)
            }
        }
    }

    fun compileApk(): ByteArray {
        val resultBase64 = nativeCompilePackage(
            androidManifest,
            resources.toTypedArray(),
        )
        return Base64.decode(resultBase64, Base64.DEFAULT)
    }

    companion object {
        init {
            System.loadLibrary("pack_java")
        }

        @JvmStatic
        private external fun nativeCompilePackage(
            androidManifest: String,
            resources: Array<Resource>,
        ): String
    }
}
