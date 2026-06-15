package voice.recorder.recordingvoice.cct.core.datastore.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import voice.recorder.recordingvoice.cct.core.datastore.AudioSettingsDataStore

val dataStoreModule = module {
    single { AudioSettingsDataStore(androidContext()) }
}
