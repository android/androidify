#!/bin/bash
# Copyright 2025 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

cd pack-java
cargo build --release --target aarch64-linux-android && \
cargo build --release --target x86_64-linux-android && \
cargo build --release --target armv7-linux-androideabi && \
cargo build --release --target i686-linux-android && \
\
mkdir -p ../src/main/jniLibs/arm64-v8a/ && \
cp ./target/aarch64-linux-android/release/libpack_java.so ../src/main/jniLibs/arm64-v8a/libpack_java.so && \
mkdir -p ../src/main/jniLibs/x86_64/ && \
cp ./target/x86_64-linux-android/release/libpack_java.so ../src/main/jniLibs/x86_64/libpack_java.so && \
mkdir -p ../src/main/jniLibs/armeabi-v7a/ && \
cp ./target/armv7-linux-androideabi/release/libpack_java.so ../src/main/jniLibs/armeabi-v7a/libpack_java.so && \
mkdir -p ../src/main/jniLibs/x86/ && \
cp ./target/i686-linux-android/release/libpack_java.so ../src/main/jniLibs/x86/libpack_java.so && \
\
echo "Compiled and saved API for Android ARM32, ARM64, x86 and x86_64"
