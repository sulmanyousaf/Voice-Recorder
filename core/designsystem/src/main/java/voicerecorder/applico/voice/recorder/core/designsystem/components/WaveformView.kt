package voicerecorder.applico.voice.recorder.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun WaveformView(
    amplitudes: FloatArray,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    onWaveformClick: ((index: Int) -> Unit)? = null
) {
    val listState = rememberLazyListState()

    // Auto-scroll to the end when a new amplitude is added
    LaunchedEffect(amplitudes.size) {
        if (amplitudes.isNotEmpty()) {
            listState.animateScrollToItem(amplitudes.lastIndex)
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth().height(120.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(amplitudes.size) { index ->
            val amplitude = amplitudes[index]
            val barHeight = 120.dp * amplitude.coerceIn(0.02f, 1.0f)

            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(6.dp)
                    .height(barHeight)
                    .background(color)
                    .clickable(enabled = onWaveformClick != null) {
                        onWaveformClick?.invoke(index)
                    }
            )
        }
    }
}
