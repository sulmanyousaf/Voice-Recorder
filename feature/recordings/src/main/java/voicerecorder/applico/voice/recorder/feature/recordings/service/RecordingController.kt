package voicerecorder.applico.voice.recorder.feature.recordings.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import voicerecorder.applico.voice.recorder.core.overlay.SnackbarManager
import voicerecorder.applico.voice.recorder.core.permissions.R as PermissionsR

interface RecordingController {
    fun startRecording()
    fun pauseRecording()
    fun resumeRecording()
    fun stopRecording()
}

class RecordingControllerImpl(
    private val context: Context,
    private val snackbarManager: SnackbarManager
) : RecordingController {
    override fun startRecording() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            snackbarManager.showMessage(PermissionsR.string.error_permission_microphone)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                snackbarManager.showMessage(PermissionsR.string.error_permission_notification)
                return
            }
        }

        VoiceRecorderService.start(context)
    }

    override fun pauseRecording() {
        VoiceRecorderService.pause(context)
    }

    override fun resumeRecording() {
        VoiceRecorderService.resume(context)
    }

    override fun stopRecording() {
        VoiceRecorderService.stop(context)
    }
}
