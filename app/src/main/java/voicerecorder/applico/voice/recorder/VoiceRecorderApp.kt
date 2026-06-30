package voicerecorder.applico.voice.recorder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.koin.compose.koinInject
import voicerecorder.applico.voice.recorder.core.designsystem.components.PermissionRationaleDialog
import voicerecorder.applico.voice.recorder.core.designsystem.theme.LocalDimensions
import voicerecorder.applico.voice.recorder.core.overlay.OverlayManager
import voicerecorder.applico.voice.recorder.core.overlay.SnackbarManager
import voicerecorder.applico.voice.recorder.core.overlay.model.Overlay
import voicerecorder.applico.voice.recorder.core.permissions.rememberMicrophonePermissionHandler
import voicerecorder.applico.voice.recorder.feature.recordings.service.RecordingController

@Composable
fun VoiceRecorderApp(
    windowSizeClass: WindowSizeClass,
    overlayManager: OverlayManager = koinInject(),
    snackbarManager: SnackbarManager = koinInject(),
    recordingController: RecordingController = koinInject()
) {
    val currentOverlay by overlayManager.currentOverlay.collectAsState()
    val permissionHandler = rememberMicrophonePermissionHandler(
        overlayManager = overlayManager,
        onPermissionGranted = {},
        onPermissionDenied = {}
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val currentContext = LocalContext.current

    LaunchedEffect(snackbarManager) {
        snackbarManager.messages.collect { message ->
            val text = message.messageText ?: message.messageResId?.let { currentContext.getString(it) } ?: ""
            val actionLabel = message.actionLabelText ?: message.actionLabelResId?.let { currentContext.getString(it) }
            snackbarHostState.showSnackbar(message = text, actionLabel = actionLabel)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Greeting(name = "Android (Size: ${windowSizeClass.widthSizeClass})")
            Button(
                onClick = { permissionHandler() },
                modifier = Modifier.padding(top = LocalDimensions.current.spacingMedium)
            ) {
                Text(stringResource(id = R.string.request_microphone_permission))
            }
            Button(
                onClick = { snackbarManager.showMessage(R.string.app_name) },
                modifier = Modifier.padding(top = LocalDimensions.current.spacingMedium)
            ) {
                Text("Test Snackbar")
            }

            Button(
                onClick = {
                    val nsAvailable = android.media.audiofx.NoiseSuppressor.isAvailable()
                    val aecAvailable = android.media.audiofx.AcousticEchoCanceler.isAvailable()
                    val msg = "Hardware Support -> Noise Suppressor: $nsAvailable, Echo Canceler: $aecAvailable"
                    snackbarManager.showMessage(msg)
                },
                modifier = Modifier.padding(top = LocalDimensions.current.spacingMedium)
            ) {
                Text("Check Audio Hardware")
            }
            
            // --- Temporary Foreground Service Testing Buttons ---
            Button(
                onClick = { recordingController.startRecording() },
                modifier = Modifier.padding(top = LocalDimensions.current.spacingMedium)
            ) {
                Text("Start Service")
            }
            Button(
                onClick = { recordingController.pauseRecording() },
                modifier = Modifier.padding(top = LocalDimensions.current.spacingMedium)
            ) {
                Text("Pause")
            }
            Button(
                onClick = { recordingController.resumeRecording() },
                modifier = Modifier.padding(top = LocalDimensions.current.spacingMedium)
            ) {
                Text("Resume")
            }
            Button(
                onClick = { recordingController.stopRecording() },
                modifier = Modifier.padding(top = LocalDimensions.current.spacingMedium)
            ) {
                Text("Stop Service")
            }
        }
    }

    when (val overlay = currentOverlay) {
        is Overlay.PermissionRationale -> {
            PermissionRationaleDialog(
                title = overlay.title,
                description = overlay.description,
                iconRes = overlay.iconRes,
                onProceed = overlay.onProceed,
                onDismiss = overlay.onDismiss
            )
        }
        else -> {
            // Other overlays can be handled here
        }
    }
}
