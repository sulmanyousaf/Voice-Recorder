package voicerecorder.applico.voice.recorder.core.datastore.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import voicerecorder.applico.voice.recorder.core.datastore.AudioSettingsDataStore

val dataStoreModule = module {
    single { AudioSettingsDataStore(androidContext()) }
}
