package voicerecorder.applico.voice.recorder.core.common.di

import org.koin.dsl.module
import voicerecorder.applico.voice.recorder.core.common.dispatchers.DefaultDispatcherProvider
import voicerecorder.applico.voice.recorder.core.common.dispatchers.DispatcherProvider

val commonModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
}
