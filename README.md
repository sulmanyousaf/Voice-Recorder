# Voice Recorder

A modern, modular Android application for recording high-quality voice notes. Built with Clean Architecture, Jetpack Compose, and Kotlin Coroutines.

## 🏗 Architecture & Structure

The project is heavily modularized to ensure separation of concerns and fast build times.

### Modules
- **`app`**: The main application module containing the entry point and dependency injection setup.
- **`build-logic`**: Contains Gradle convention plugins (`vr.android.application`, `vr.android.library`, etc.) to centralize build configurations and dependencies.
- **`core:designsystem`**: Houses the UI theme, typography, colors, and reusable composables.
- **`core:media`**: The audio engine. Handles `AudioRecord`, encoders (`WavEncoder`, `AacEncoder`), and `AndroidRecordingStorage` for MediaStore interactions.
- **`core:notifications`**: Manages all notification channels, active recording MediaStyle notifications, and daily reminders using AlarmManager.
- **`core:permissions`**: Provides centralized string resources and logic for strict runtime permission handling.
- **`data:recordings`**: The data layer containing the Room database (`RecordingDatabase`), DAOs, and the repository (`RecordingRepositoryImpl`) for persisting recording metadata.
- **`feature:recordings`**: The active recording domain. Houses the foreground `VoiceRecorderService`, `AudioFocusManager` for managing audio interruptions, and `RecordingControllerImpl` for state and permission validation.

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

1. **Foreground Audio Engine**: 
   Records raw PCM audio data via `AudioRecord` inside a resilient `VoiceRecorderService` to prevent the OS from killing it. It respects `AudioFocus`, pausing automatically during phone calls or external media interruptions.
2. **Storage Management**: 
   Audio files are initially saved to the cache directory (`getTempFile`). Upon completion, they are securely moved to the public Android `Recordings/VoiceRecorder` directory using the `MediaStore` API.
3. **Strict Validation & Error Handling**:
   The `RecordingController` acts as a strict gatekeeper, ensuring `RECORD_AUDIO` and `POST_NOTIFICATIONS` are granted before allocating resources. Disk operations are wrapped in Kotlin `runCatching` blocks, and errors are elegantly pushed to a global `SnackbarManager`.
4. **Digital Signal Processing (DSP)**:
   The audio engine intercepts the raw PCM byte stream to apply real-time math: extracting live amplitude for UI waveforms, digitally boosting microphone gain (200%), and seamlessly skipping dead air with voice activity detection and a 1-second hold-time delay to preserve natural speech cadence.
5. **Build Logic**: 
   Instead of copy-pasting Gradle code, the project uses convention plugins (e.g. `id("vr.android.library")`) to enforce uniform compiler options, Jetpack Compose settings, and Detekt analysis across all modules.

---
*Note: This README must be kept updated alongside major architectural or dependency changes.*
