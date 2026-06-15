package voice.recorder.recordingvoice.cct.core.media.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import voice.recorder.recordingvoice.cct.core.media.playback.PlaybackEngine
import voice.recorder.recordingvoice.cct.core.media.recording.AudioRecordEngine
import voice.recorder.recordingvoice.cct.core.media.storage.AndroidRecordingStorage
import voice.recorder.recordingvoice.cct.core.media.storage.RecordingStorage

val mediaModule = module {
    single { AudioRecordEngine() }
    single<RecordingStorage> { AndroidRecordingStorage(androidContext()) }
    single { PlaybackEngine(androidContext()) }
}
