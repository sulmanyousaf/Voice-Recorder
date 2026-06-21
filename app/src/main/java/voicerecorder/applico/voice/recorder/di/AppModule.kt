package voicerecorder.applico.voice.recorder.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import android.content.Intent
import voicerecorder.applico.voice.recorder.MainActivity
import voicerecorder.applico.voice.recorder.core.shortcuts.VoiceRecorderShortcutManager
import voicerecorder.applico.voice.recorder.feature.recordings.viewmodel.RecordingsViewModel

val appModule = module {
    single { 
        VoiceRecorderShortcutManager(
            applicationContext = androidContext(),
            targetIntentFactory = { Intent(androidContext(), MainActivity::class.java) }
        ) 
    }
    viewModelOf(::RecordingsViewModel)
}
