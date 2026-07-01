package voicerecorder.applico.voice.recorder.data.recordings.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import voicerecorder.applico.voice.recorder.data.recordings.repository.RecordingRepository
import voicerecorder.applico.voice.recorder.data.recordings.repository.RecordingRepositoryImpl
import voicerecorder.applico.voice.recorder.data.recordings.scanner.MediaStoreScanner

import voicerecorder.applico.voice.recorder.data.recordings.draft.DraftManager
import voicerecorder.applico.voice.recorder.data.recordings.draft.DraftManagerImpl

val recordingsDataModule = module {
    single { MediaStoreScanner(androidContext()) }
    single<RecordingRepository> { RecordingRepositoryImpl(get(), get(), get()) }
    single<DraftManager> { DraftManagerImpl(androidContext()) }
}
