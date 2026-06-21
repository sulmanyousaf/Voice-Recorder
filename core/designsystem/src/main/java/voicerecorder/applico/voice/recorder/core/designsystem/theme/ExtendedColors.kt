package voicerecorder.applico.voice.recorder.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class VoiceRecorderExtendedColors(
    val recording: Color,
    val waveform: Color,
    val warning: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    VoiceRecorderExtendedColors(
        recording = Color.Unspecified,
        waveform = Color.Unspecified,
        warning = Color.Unspecified
    )
}
