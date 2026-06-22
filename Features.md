# Features Tracker

This document tracks all completed features of the Voice Recorder app and outlines the roadmap for upcoming additions and professional improvements.

## ✅ Completed Features (Backend & Logic)

1. **Audio Recording Engine**
   - High-performance raw PCM audio capture with live encoding to WAV and AAC/MP4.
   - Resilient `VoiceRecorderService` (Foreground Service) ensuring the OS never kills the app.
   - `AudioFocusManager` handles phone calls and external media interruptions gracefully.
2. **Storage & MediaStore Integration**
   - Temporary cache storage during active recording.
   - Final export to the public `Recordings/VoiceRecorder` directory via Android `MediaStore`.
   - Robust error handling (`runCatching`) for disk-full or permission-denied scenarios.
3. **Database Integration**
   - Room database tracking recording metadata (Name, Duration, Size, Path, URI).
   - Clean Architecture Repository (`RecordingRepositoryImpl`) bridging data and domain layers.
4. **Notifications**
   - **Active Recording**: Interactive `MediaStyle` pinned notification (Play/Pause/Stop controls).
   - **Daily Reminders**: Scheduled notifications to encourage user engagement (Morning/Evening).
5. **Strict Permission Handling**
   - Graceful runtime checks for `RECORD_AUDIO` and `POST_NOTIFICATIONS` (Android 13+).
   - Elegant error propagation to the UI via a global `SnackbarManager` (Event Bus).
6. **Project Infrastructure**
   - Fully modularized architecture (Core, Data, UI).
   - Dependency Injection via Koin.
   - Static Code Analysis via Detekt.
7. **Audio Engine DSP Math (Phase 2)**
    - Real-time Amplitude & Waveform Calculation in the PCM byte stream.
    - Digital Signal Amplifier (Mic Boost Logic).
    - Voice Activity Detection (Silence Skipping with Hold Time).

---

## 🚧 Upcoming Features (To-Do)

1. **User Interface (Jetpack Compose)**
   - Main screen listing all recordings.
   - Active recording screen with live waveform visualizer.
   - Settings screen to choose recording format (WAV vs AAC).
2. **Meaningful Unit Tests**
   - Mocking the audio byte stream to test the `WavEncoder` and `AacEncoder`.
   - Testing `RecordingRepositoryImpl` mapping logic.

---

## 🚀 Future Enhancements (Suggestions to Beat Top Apps)

To stand out in the Google Play Store and compete with top-tier voice recorder apps, we should eventually implement the following:

1. **Cloud Sync & Backup**
   - Integrate Google Drive API to automatically back up recordings to the cloud so users never lose their data.
2. **Audio Transcription (Speech-to-Text)**
   - Use Android's built-in `SpeechRecognizer` or an external API (like OpenAI Whisper) to generate live transcripts of the recordings.
3. **Audio Trimming & Editing**
   - Allow users to crop silence out of the beginning/end of their recordings right inside the app.
4. **Audio Bookmarking (Pins)**
   - Let users tap a "Pin" button while recording to drop a timestamp marker (e.g., "Important point at 04:12").
5. **Premium Theming**
   - Material You (Dynamic Colors) support.
   - Pitch-black AMOLED dark mode to save battery while recording screen-on.
6. **Privacy & Password Protection**
   - Implement biometric (fingerprint/face) or PIN lock to protect sensitive recordings from unauthorized access.
7. **Set as Default App**
   - Support `android.provider.MediaStore.RECORD_SOUND` intents so users can launch our app directly from other apps (like Messaging apps) to record audio.
8. **Noise Reduction & Echo Cancellation**
   - Implement Android's hardware `NoiseSuppressor` and `AcousticEchoCanceler` to improve audio clarity in noisy environments.
9. **Wear OS Companion App**
   - Build a standalone smartwatch interface to start/stop recordings remotely from the user's wrist.
10. **Scheduled Recording**
    - Allow users to set a specific date and time for the app to automatically wake up and start recording (e.g., for lectures or meetings).
