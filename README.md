# Voice Recorder

A modern, modular Android application for recording high-quality voice notes. Built with Clean Architecture, Jetpack Compose, and Kotlin Coroutines.

## 🏗 Architecture & Structure

The project is heavily modularized to ensure separation of concerns and fast build times.

### Modules
- **`app`**: The main application module containing the entry point and dependency injection setup.
- **`build-logic`**: Contains Gradle convention plugins (`vr.android.application`, `vr.android.library`, etc.) to centralize build configurations and dependencies.
- **`core:designsystem`**: Houses the UI theme, typography, colors, and reusable composables.
- **`core:media`**: The audio engine. Handles `AudioRecord`, encoders (`WavEncoder`, `AacEncoder`), background `RecordingService`, and `AndroidRecordingStorage` for MediaStore interactions.
- **`core:notifications`**: Manages all notification channels, active recording pinned notifications, and daily reminders using AlarmManager.
- **`data:recordings`**: The data layer containing the Room database (`RecordingDatabase`), DAOs, and the repository (`RecordingRepositoryImpl`) for persisting recording metadata.

## 🛠 Tech Stack & Versions

- **Minimum SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 37
- **Compile SDK**: 37
- **Java Version**: 21
- **Language**: Kotlin `2.4.0`
- **UI Framework**: Jetpack Compose (BOM `2026.06.00`)
- **Native Toolchain**: NDK `30.0.14904198`, CMake `4.1.2`
- **Dependency Injection**: Koin `4.2.2`
- **Database**: Room `2.8.4`
- **Concurrency**: Kotlin Coroutines & Flow
- **Media**: Media3 `1.10.1`
- **Static Analysis**: Detekt `1.23.8`
- **Testing**: JUnit 4, MockK `1.14.11`, Coroutines Test `1.11.0`
- **Build System**: Gradle Version Catalogs (`libs.versions.toml`)

## 💡 Implementation Details

1. **Audio Engine**: 
   Records raw PCM audio data via `AudioRecord` and streams it to specialized encoders. This prevents memory `OutOfMemory` exceptions on long recordings.
2. **Storage Management**: 
   Audio files are initially saved to the cache directory (`getTempFile`). Upon completion, they are securely moved to the public Android `Recordings/VoiceRecorder` directory using the `MediaStore` API.
3. **Error Handling**:
   Disk operations are wrapped in Kotlin `runCatching` blocks, returning `Result` states so the UI and service layer can gracefully abort if storage is full or permissions are missing.
4. **Build Logic**: 
   Instead of copy-pasting Gradle code, the project uses convention plugins (e.g. `id("vr.android.library")`) to enforce uniform compiler options, Jetpack Compose settings, and Detekt analysis across all modules.

---
*Note: This README must be kept updated alongside major architectural or dependency changes.*
