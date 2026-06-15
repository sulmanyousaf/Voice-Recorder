package voicerecorder.applico.voice.recorder.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Dimensions(
    val defaultPadding: Dp,
    val controlBarHeight: Dp,
    val waveformMinHeight: Dp,
    val waveformMaxHeight: Dp,
    val listSpacing: Dp
)

val CompactDimensions = Dimensions(
    defaultPadding = 16.dp,
    controlBarHeight = 80.dp,
    waveformMinHeight = 80.dp,
    waveformMaxHeight = 160.dp,
    listSpacing = 8.dp
)

val ExpandedDimensions = Dimensions(
    defaultPadding = 24.dp,
    controlBarHeight = 100.dp,
    waveformMinHeight = 120.dp,
    waveformMaxHeight = 240.dp,
    listSpacing = 16.dp
)

val LocalDimensions = staticCompositionLocalOf { CompactDimensions }
