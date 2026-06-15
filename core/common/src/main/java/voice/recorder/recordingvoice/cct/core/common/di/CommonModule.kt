package voice.recorder.recordingvoice.cct.core.common.di

import org.koin.dsl.module
import voice.recorder.recordingvoice.cct.core.common.dispatchers.DefaultDispatcherProvider
import voice.recorder.recordingvoice.cct.core.common.dispatchers.DispatcherProvider

val commonModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
}
