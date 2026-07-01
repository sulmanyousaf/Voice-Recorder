package voicerecorder.applico.voice.recorder.core.media.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import voicerecorder.applico.voice.recorder.core.media.playback.PlaybackEngine
import voicerecorder.applico.voice.recorder.core.media.recording.AudioRecordEngine
import voicerecorder.applico.voice.recorder.core.media.recording.RecordingServiceManager
import voicerecorder.applico.voice.recorder.core.media.storage.AndroidRecordingStorage
import voicerecorder.applico.voice.recorder.core.media.storage.RecordingStorage
import voicerecorder.applico.voice.recorder.core.media.storage.AudioTagger
import voicerecorder.applico.voice.recorder.core.media.storage.AudioTaggerImpl

val mediaModule = module {
    single { AudioRecordEngine() }
    single<RecordingStorage> { AndroidRecordingStorage(androidContext()) }
    single { PlaybackEngine(androidContext()) }
    single { RecordingServiceManager() }
    single<AudioTagger> { AudioTaggerImpl() }
}
