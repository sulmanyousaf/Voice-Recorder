package voice.recorder.recordingvoice.cct.data.recordings.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import voice.recorder.recordingvoice.cct.data.recordings.repository.RecordingRepository
import voice.recorder.recordingvoice.cct.data.recordings.repository.RecordingRepositoryImpl
import voice.recorder.recordingvoice.cct.data.recordings.scanner.MediaStoreScanner

val recordingsDataModule = module {
    single { MediaStoreScanner(androidContext()) }
    single<RecordingRepository> { RecordingRepositoryImpl(get(), get(), get()) }
}
