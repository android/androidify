#!/usr/bin/env bash
#
# Copyright 2025 The Android Open Source Project
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

# IGNORE this file, it's only used in the internal Google release process
# Fail on any error to ensure the script stops if a step fails.
set -e

# --- Configuration ---
# Get the script's directory.
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo DIR
# Define the Android SDK version you want to target.
ANDROID_SDK_VERSION="37.0"
ANDROID_BUILD_TOOLS_VERSION="37.0.0"

# Switched from 'google_apis' to 'google_atd' (Google Automated Test Device).
# This system image is designed for headless, automated testing in CI environments
# and is more compatible with software rendering. It will be installed but may not
# be used by the new build command.
# 37 not available yet as per b/432143095
EMULATOR_IMAGE="system-images;android-35;google_atd;x86_64"

# --- Environment Setup ---

# Step 1: Check for essential command-line tools.
echo "INFO: Checking for prerequisites (wget, unzip, tar)..."
for cmd in wget unzip tar; do
  if ! command -v $cmd &> /dev/null; then
    echo "ERROR: Command '$cmd' not found. Please install it using your system's package manager (e.g., 'sudo apt-get install $cmd') and try again."
    exit 1
  fi
done
echo "INFO: Prerequisites are installed."


# Step 2: Install and configure Java 17 system-wide.
echo "INFO: Setting up Java 17..."
# The build needs Java 17, set it as the default Java version.
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk
sudo update-alternatives --set java /usr/lib/jvm/java-17-openjdk-amd64/bin/java
java -version

# Also clear JAVA_HOME variable so java -version is used instead
export JAVA_HOME=

# Add the local SDK and emulator tools to the PATH for this session.
# The system-wide Java will already be in the PATH.
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator"
echo "INFO: Local tools added to PATH."

# Now, accept licenses and install packages.
# It's best practice to accept licenses *after* the tools are in place.
echo "INFO: Accepting all pending SDK licenses..."
yes | sdkmanager --licenses

echo "INFO: Installing Android SDK packages, including emulator and system image..."
# This single command will install/update all necessary packages.
sdkmanager "platforms;android-${ANDROID_SDK_VERSION}" "build-tools;${ANDROID_BUILD_TOOLS_VERSION}" "platform-tools" "${EMULATOR_IMAGE}" "emulator"

# Run license acceptance AGAIN after installing new packages. This is crucial.
echo "INFO: Accepting licenses for newly installed packages..."
yes | sdkmanager --licenses

echo "Copying google-services.json"
cp /tmpfs/src/git/androidify-prebuilts/google-services.json ${DIR}/app

echo "Copying gradle.properties"
echo "" >> ${DIR}/gradle.properties # add a new line to the file
cat /tmpfs/src/git/androidify-prebuilts/gradle.properties >> ${DIR}/gradle.properties
ls

# --- Build Process ---

# This script assembles the release build of the Android application.
# Ensure gradlew is executable
chmod +x ./gradlew

# Clean the project (optional, but good for a fresh release build)
echo "INFO: Cleaning the project..."
./gradlew clean -Pandroid.sdk.path=$ANDROID_HOME

# Build the production release bundles without generating baseline profiles.
echo "INFO: Building the Android production release bundle..."
./gradlew app:bundleRelease app:spdxSbomForRelease -x test -x uploadCrashlyticsMappingFileRelease -Pandroid.sdk.path=$ANDROID_HOME -PCI_BUILD=true

# --- Artifact Collection ---
echo "INFO: Preparing artifacts for Kokoro..."

# This function collects a specific AAB and its associated in-toto files.
# Arguments:
#   $1: Source directory for the AAB (e.g., "app/build/outputs/bundle/release")
#   $2: Source filename for the AAB (e.g., "app-release.aab")
#   $3: Destination filename for the AAB (e.g., "app-release-unsigned.aab")
collect_artifacts() {
  local aab_src_dir="$1"
  local aab_file="$2"
  local aab_dest_file="$3"
  local aab_path="${aab_src_dir}/${aab_file}"

  # Check if the AAB exists
  if [[ -f "$aab_path" ]]; then
    # Create a directory within Kokoro's artifact collection area
    local artifact_dest_dir="${KOKORO_ARTIFACTS_DIR}/artifacts"
    mkdir -p "${artifact_dest_dir}"

    # Copy the AAB
    cp "${aab_path}" "${artifact_dest_dir}/${aab_dest_file}"
    echo "SUCCESS: AAB copied to ${artifact_dest_dir}"

  else
    echo "FAILURE: AAB not found at ${aab_path}"
    exit 1
  fi
}

# Collect the main application artifacts
collect_artifacts "app/build/outputs/bundle/release" "app-release.aab" "app-release-unsigned.aab"

# Copy the app-specific SPDX SBOM
echo "INFO: Copying SPDX SBOM..."
cp app/build/spdx/release.spdx.json "${KOKORO_ARTIFACTS_DIR}/artifacts/app-release.spdx.json"

exit 0