package voicerecorder.applico.voice.recorder.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Dimensions(
    val defaultPadding: Dp,
    val controlBarHeight: Dp,
    val waveformMinHeight: Dp,
    val waveformMaxHeight: Dp,
    val listSpacing: Dp,
    val spacingMedium: Dp,
    val iconSizeLarge: Dp,
    val dialogElevation: Dp,
    val dialogCornerRadius: Dp
)

val CompactDimensions = Dimensions(
    defaultPadding = 16.dp,
    controlBarHeight = 80.dp,
    waveformMinHeight = 80.dp,
    waveformMaxHeight = 160.dp,
    listSpacing = 8.dp,
    spacingMedium = 16.dp,
    iconSizeLarge = 48.dp,
    dialogElevation = 6.dp,
    dialogCornerRadius = 28.dp
)

val ExpandedDimensions = Dimensions(
    defaultPadding = 24.dp,
    controlBarHeight = 100.dp,
    waveformMinHeight = 120.dp,
    waveformMaxHeight = 240.dp,
    listSpacing = 16.dp,
    spacingMedium = 24.dp,
    iconSizeLarge = 64.dp,
    dialogElevation = 8.dp,
    dialogCornerRadius = 32.dp
)

val MediumDimensions = Dimensions(
    defaultPadding = 20.dp,
    controlBarHeight = 90.dp,
    waveformMinHeight = 100.dp,
    waveformMaxHeight = 200.dp,
    listSpacing = 12.dp,
    spacingMedium = 20.dp,
    iconSizeLarge = 56.dp,
    dialogElevation = 7.dp,
    dialogCornerRadius = 30.dp
)

val LocalDimensions = staticCompositionLocalOf { CompactDimensions }
