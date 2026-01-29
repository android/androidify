# AGENTS.md

## Project Overview
**Androidify** is an Android application that allows users to create custom Android bot avatars using AI. It leverages modern Android technologies and integrates with Google's Gemini models for generative AI features.

## Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM / Clean Architecture guidelines (implied by `core`, `feature`, `data` structure)
- **AI/ML**: 
    - Gemini API (via Firebase AI Logic SDK)
    - Imagen models
- **Camera**: CameraX
- **Navigation**: Navigation 3 (Compose)
- **Dependency Injection**: Hilt (implied by `libs.plugins.hilt` in build.gradle)
- **Build System**: Gradle (Kotlin DSL)

## Project Structure
The project follows a modularized architecture:

- **`/app`**: The main Android application module. Connects features and configures the app.
- **`/core`**: Core libraries and utilities shared across modules (e.g., `core/network`, `core/util`).
- **`/feature`**: Feature-specific modules (e.g., `feature/home`, `feature/creation`).
- **`/data`**: Data layer repositories and datasources.
- **`/wear`**: Wear OS specific module.
- **`/watchface`**: Watch face module for Wear OS.
- **`/setup`**: Scripts and configuration files for setting up the project (e.g., Firebase Remote Config).

## Development Workflow

### Prerequisites
- Android Studio (Latest Stable or Preview as needed for bleeding edge features)
- JDK 17+ (Standard for modern Android dev)
- `google-services.json` placed in `/app` (See `README.md` for setup).

### Common Commands

**Build:**
```bash
./gradlew assembleDebug
```

**Test:**
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

**Code Formatting & Linting:**
This project uses **Spotless** for code formatting.

```bash
# Check for formatting issues
./gradlew spotlessCheck

# Apply formatting fixes
./gradlew spotlessApply
```

### Configuration
- **Firebase**: Requires a valid `google-services.json`.
- **Remote Config**: Defaults found in `core/network/src/main/res/xml/remote_config_defaults.xml`.
- **API Keys**: Managed via `local.properties` or build config fields (check specific module `build.gradle.kts` for details).

## Key Guidelines for Agents
1. **Compose First**: adhere to modern Jetpack Compose best practices.
2. **Modularization**: Respect the module boundaries. Core modules should not depend on features.
3. **Spotless**: Always run `spotlessApply` on modified files if possible, or ensure code aligns with the project's style guide.
4. **AI Features**: When working on AI features, refer to the Firebase AI Logic SDK documentation and established patterns in the codebase.
5. **Large Screen**: The app is a Tier 1 example of Large screen guidelines, meaning its fully optimized for different screen sizes. When performing any changes, ensure it works across the specrtrum of Android devices. 
