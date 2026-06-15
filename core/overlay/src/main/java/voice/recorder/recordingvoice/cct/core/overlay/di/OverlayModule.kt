package voice.recorder.recordingvoice.cct.core.overlay.di

import org.koin.dsl.module
import voice.recorder.recordingvoice.cct.core.overlay.OverlayManager

val overlayModule = module {
    single { OverlayManager() }
}
