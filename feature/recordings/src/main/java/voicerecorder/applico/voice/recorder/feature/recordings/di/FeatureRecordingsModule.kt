package voicerecorder.applico.voice.recorder.feature.recordings.di

import org.koin.dsl.module
import voicerecorder.applico.voice.recorder.feature.recordings.service.RecordingController
import voicerecorder.applico.voice.recorder.feature.recordings.service.RecordingControllerImpl

val featureRecordingsModule = module {
    viewModel { RecordingsViewModel(get(), get(), get()) }
    single<RecordingController> { RecordingControllerImpl(get(), get(), get(), get()) }
}
