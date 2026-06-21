package voicerecorder.applico.voice.recorder.core.overlay.model

sealed interface Overlay {
    data class PermissionRationale(
        val permission: String,
        val title: String,
        val description: String,
        val iconRes: Int?,
        val onProceed: () -> Unit,
        val onDismiss: () -> Unit
    ) : Overlay
    data class TagEditor(val recordingId: String, val onDismiss: () -> Unit) : Overlay
    data class FormatSelector(val onFormatSelected: (String) -> Unit, val onDismiss: () -> Unit) : Overlay
    data class ErrorAlert(val message: String, val onDismiss: () -> Unit) : Overlay
}
