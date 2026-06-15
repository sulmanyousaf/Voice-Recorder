package voicerecorder.applico.voice.recorder.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

val MaterialTheme.dimens: Dimensions
    @Composable
    @ReadOnlyComposable
    get() = LocalDimensions.current
