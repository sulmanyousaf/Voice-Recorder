package voicerecorder.applico.voice.recorder.core.overlay

import androidx.annotation.StringRes
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class SnackbarMessage(
    @StringRes val messageResId: Int,
    @StringRes val actionLabelResId: Int? = null
)

class SnackbarManager {
    private val _messages = MutableSharedFlow<SnackbarMessage>(extraBufferCapacity = 1)
    val messages = _messages.asSharedFlow()

    fun showMessage(@StringRes messageResId: Int, @StringRes actionLabelResId: Int? = null) {
        _messages.tryEmit(SnackbarMessage(messageResId, actionLabelResId))
    }
}
