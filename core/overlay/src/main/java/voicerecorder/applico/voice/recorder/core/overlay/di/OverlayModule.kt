package voicerecorder.applico.voice.recorder.core.overlay.di

import org.koin.dsl.module
import voicerecorder.applico.voice.recorder.core.overlay.OverlayManager

val overlayModule = module {
    single { OverlayManager() }
}
