package voice.recorder.recordingvoice.cct.core.overlay

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import voice.recorder.recordingvoice.cct.core.overlay.model.Overlay
import java.util.LinkedList
import java.util.Queue

class OverlayManager {
    private val overlayQueue: Queue<Overlay> = LinkedList()
    private val _currentOverlay = MutableStateFlow<Overlay?>(null)
    val currentOverlay: StateFlow<Overlay?> = _currentOverlay.asStateFlow()

    fun show(overlay: Overlay) {
        synchronized(this) {
            if (_currentOverlay.value == null) {
                _currentOverlay.value = overlay
            } else {
                overlayQueue.offer(overlay)
            }
        }
    }

    fun dismiss() {
        synchronized(this) {
            if (overlayQueue.isNotEmpty()) {
                _currentOverlay.value = overlayQueue.poll()
            } else {
                _currentOverlay.value = null
            }
        }
    }
}
