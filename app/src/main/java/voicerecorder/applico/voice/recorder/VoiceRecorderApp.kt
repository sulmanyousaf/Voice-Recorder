package voicerecorder.applico.voice.recorder

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.androidx.compose.koinViewModel
import voicerecorder.applico.voice.recorder.core.designsystem.components.DraftRecoveryDialog
import voicerecorder.applico.voice.recorder.core.designsystem.components.PermissionRationaleDialog
import voicerecorder.applico.voice.recorder.data.recordings.draft.DraftManager
import voicerecorder.applico.voice.recorder.core.overlay.OverlayManager
import voicerecorder.applico.voice.recorder.core.overlay.SnackbarManager
import voicerecorder.applico.voice.recorder.core.overlay.model.Overlay
import voicerecorder.applico.voice.recorder.core.permissions.rememberMicrophonePermissionHandler
import voicerecorder.applico.voice.recorder.feature.recordings.service.RecordingController
import voicerecorder.applico.voice.recorder.ui.screens.HomeScreen
import voicerecorder.applico.voice.recorder.ui.screens.RecordScreen
import voicerecorder.applico.voice.recorder.ui.screens.RecordingsListScreen
import voicerecorder.applico.voice.recorder.ui.screens.PlaybackScreen
import voicerecorder.applico.voice.recorder.feature.recordings.viewmodel.RecordingsViewModel
import voicerecorder.applico.voice.recorder.core.media.playback.PlaybackEngine
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.entryProvider

sealed class Screen {
    object Home : Screen()
    object Record : Screen()
    object List : Screen()
    data class Playback(val id: String) : Screen()
}

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
    val applicationContext = LocalContext.current.applicationContext
    val coroutineScope = rememberCoroutineScope()

    val draftManager: DraftManager = koinInject()

    // Navigation Stack (Manual fallback since NavDisplay API is experimental, but we treat it as state-based for simplicity)
    val backStack = remember { mutableStateListOf<Screen>(Screen.Home) }

    LaunchedEffect(Unit) {
        if (draftManager.hasDraft()) {
            overlayManager.show(
                Overlay.DraftRecovery(
                    onResume = {
                        recordingController.resumeDraft()
                        overlayManager.dismiss()
                        backStack.add(Screen.Record)
                    },
                    onSave = {
                        recordingController.saveRecording()
                        overlayManager.dismiss()
                    },
                    onDiscard = {
                        coroutineScope.launch {
                            draftManager.discardDraft()
                            overlayManager.dismiss()
                        }
                    }
                )
            )
        }
    }

    LaunchedEffect(snackbarManager) {
        snackbarManager.messages.collect { message ->
            val text = applicationContext.getString(message.messageResId)
            val actionLabel = message.actionLabelResId?.let { applicationContext.getString(it) }
            snackbarHostState.showSnackbar(message = text, actionLabel = actionLabel)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        
        NavDisplay(
            backStack = backStack,
            onBack = { if (backStack.isNotEmpty()) backStack.removeAt(backStack.lastIndex) },
            entryProvider = entryProvider {
                entry<Screen.Home> {
                    HomeScreen(
                        onNavigateToRecord = { backStack.add(Screen.Record) },
                        onNavigateToList = { backStack.add(Screen.List) },
                        onNavigateToPlayback = { backStack.add(Screen.List) },
                        onTestPermissions = { permissionHandler() },
                        onTestHardware = {
                            val nsAvailable = android.media.audiofx.NoiseSuppressor.isAvailable()
                            val aecAvailable = android.media.audiofx.AcousticEchoCanceler.isAvailable()
                            val msgRes = if (nsAvailable && aecAvailable) R.string.hardware_support_both
                            else if (!nsAvailable && !aecAvailable) R.string.hardware_support_none
                            else if (nsAvailable) R.string.hardware_support_ns_only
                            else R.string.hardware_support_aec_only
                            snackbarManager.showMessage(msgRes)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                entry<Screen.Record> {
                    RecordScreen(
                        recordingController = recordingController,
                        onNavigateBack = { if (backStack.isNotEmpty()) backStack.removeAt(backStack.lastIndex) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                entry<Screen.List> {
                    val recordingsViewModel: RecordingsViewModel = koinViewModel()
                    RecordingsListScreen(
                        viewModel = recordingsViewModel,
                        onNavigateToPlayback = { id -> backStack.add(Screen.Playback(id)) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                entry<Screen.Playback> { key ->
                    val recordingsViewModel: RecordingsViewModel = koinViewModel()
                    val playbackEngine: PlaybackEngine = koinInject()
                    PlaybackScreen(
                        recordingId = key.id,
                        viewModel = recordingsViewModel,
                        playbackEngine = playbackEngine,
                        onNavigateBack = { if (backStack.isNotEmpty()) backStack.removeAt(backStack.lastIndex) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        )
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
        is Overlay.DraftRecovery -> {
            DraftRecoveryDialog(
                onResume = overlay.onResume,
                onSave = overlay.onSave,
                onDiscard = overlay.onDiscard
            )
        }
        else -> {
            // Other overlays can be handled here
        }
    }
}
