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

@Composable
fun VoiceRecorderApp(
    windowSizeClass: WindowSizeClass,
    overlayManager: OverlayManager = koinInject(),
    snackbarManager: SnackbarManager = koinInject()
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
            val text = currentContext.getString(message.messageResId)
            val actionLabel = message.actionLabelResId?.let { currentContext.getString(it) }
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
