package voicerecorder.applico.voice.recorder.core.permissions

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import voicerecorder.applico.voice.recorder.core.overlay.OverlayManager
import voicerecorder.applico.voice.recorder.core.overlay.model.Overlay
import voicerecorder.applico.voice.recorder.core.permissions.R

@Composable
fun rememberMicrophonePermissionHandler(
    overlayManager: OverlayManager,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit = {}
): () -> Unit {
    val context = LocalContext.current
    val activity = context as? Activity

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

    return {
        val permission = Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted()
        } else {
            val shouldShowRationale = activity?.let { 
                ActivityCompat.shouldShowRequestPermissionRationale(it, permission) 
            } ?: false
            
            if (shouldShowRationale) {
                overlayManager.show(
                    Overlay.PermissionRationale(
                        permission = permission,
                        title = context.getString(R.string.microphone_permission_required),
                        description = context.getString(R.string.microphone_permission_rationale),
                        iconRes = android.R.drawable.ic_btn_speak_now, // Android's default mic icon
                        onProceed = {
                            overlayManager.dismiss()
                            launcher.launch(permission)
                        },
                        onDismiss = {
                            overlayManager.dismiss()
                            onPermissionDenied()
                        }
                    )
                )
            } else {
                launcher.launch(permission)
            }
        }
    }
}
