package voicerecorder.applico.voice.recorder.core.media.recording

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class RecordingState {
    object Idle : RecordingState()
    data class Recording(val durationSeconds: Int, val amplitudes: FloatArray) : RecordingState() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Recording
            if (durationSeconds != other.durationSeconds) return false
            if (!amplitudes.contentEquals(other.amplitudes)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = durationSeconds
            result = 31 * result + amplitudes.contentHashCode()
            return result
        }
    }
    data class Paused(val durationSeconds: Int) : RecordingState()
}

/**
 * Singleton tracker to bridge the Foreground Service state to the UI layer.
 */
class RecordingServiceManager {
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    fun updateState(state: RecordingState) {
        _recordingState.value = state
    }
}
