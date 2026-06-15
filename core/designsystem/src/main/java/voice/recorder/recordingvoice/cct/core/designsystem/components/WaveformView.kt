package voice.recorder.recordingvoice.cct.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun WaveformView(
    amplitudes: FloatArray,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.fillMaxWidth().height(120.dp)) {
        val width = size.width
        val height = size.height
        val barWidth = 6f
        val space = 4f
        val maxBars = (width / (barWidth + space)).toInt()

        val data = if (amplitudes.size > maxBars) {
            amplitudes.sliceArray((amplitudes.size - maxBars) until amplitudes.size)
        } else {
            amplitudes
        }

        data.forEachIndexed { index, amplitude ->
            val barHeight = height * amplitude.coerceIn(0.02f, 1.0f)
            val x = width - (data.size - index) * (barWidth + space)
            val yStart = (height - barHeight) / 2
            val yEnd = yStart + barHeight
            
            drawLine(
                color = color,
                start = Offset(x, yStart),
                end = Offset(x, yEnd),
                strokeWidth = barWidth
            )
        }
    }
}
