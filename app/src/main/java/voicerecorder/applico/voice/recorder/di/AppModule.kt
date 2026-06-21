package voicerecorder.applico.voice.recorder.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import voicerecorder.applico.voice.recorder.shortcut.VoiceRecorderShortcutManager
import voicerecorder.applico.voice.recorder.viewmodel.RecordingsViewModel

val appModule = module {
    single { VoiceRecorderShortcutManager(androidContext()) }
    viewModelOf(::RecordingsViewModel)
}
