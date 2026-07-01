package voicerecorder.applico.voice.recorder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import voicerecorder.applico.voice.recorder.core.designsystem.components.WaveformView
import voicerecorder.applico.voice.recorder.feature.recordings.service.RecordingController
import voicerecorder.applico.voice.recorder.core.overlay.OverlayManager
import voicerecorder.applico.voice.recorder.core.overlay.model.Overlay
import org.koin.compose.koinInject
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.launch

@Composable
fun RecordScreen(
    recordingController: RecordingController,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentAmplitude by recordingController.amplitudeFlow.collectAsState(initial = 0f)
    val amplitudes = remember { mutableStateListOf<Float>() }
    val scope = rememberCoroutineScope()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinNoteText by remember { mutableStateOf("") }
    var clickedAmplitudeIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        val history = recordingController.getHistoricalAmplitudes()
        amplitudes.addAll(history)
    }

    LaunchedEffect(currentAmplitude) {
        if (currentAmplitude > 0f) {
            amplitudes.add(currentAmplitude)
        }
    }

    val overlayManager: OverlayManager = koinInject()

    BackHandler(enabled = true) {
        overlayManager.show(Overlay.DraftRecovery(
            onResume = { overlayManager.dismiss() },
            onSave = {
                recordingController.saveRecording()
                overlayManager.dismiss()
                onNavigateBack()
            },
            onDiscard = {
                recordingController.discardRecording()
                overlayManager.dismiss()
                onNavigateBack()
            }
        ))
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Recording Screen", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        WaveformView(
            amplitudes = amplitudes.toFloatArray(),
            modifier = Modifier.fillMaxWidth().height(150.dp),
            onWaveformClick = { index ->
                clickedAmplitudeIndex = index
                pinNoteText = ""
                showPinDialog = true
            }
        )
        
        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showPinDialog = false 
                    clickedAmplitudeIndex = null
                },
                title = { Text("Add Pin") },
                text = {
                    TextField(
                        value = pinNoteText,
                        onValueChange = { pinNoteText = it },
                        placeholder = { Text("Enter an optional note...") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val note = pinNoteText.takeIf { it.isNotBlank() }
                        if (clickedAmplitudeIndex != null) {
                            recordingController.pinAtAmplitudeIndex(clickedAmplitudeIndex!!, note)
                        } else {
                            recordingController.pinRecording(note = note)
                        }
                        showPinDialog = false
                        clickedAmplitudeIndex = null
                    }) {
                        Text("Add Pin")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showPinDialog = false 
                        clickedAmplitudeIndex = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = { recordingController.startRecording() }) {
                Text("Start")
            }
            Button(onClick = { recordingController.pauseRecording() }) {
                Text("Pause")
            }
            Button(onClick = { recordingController.resumeRecording() }) {
                Text("Resume")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = { 
                clickedAmplitudeIndex = null
                pinNoteText = ""
                showPinDialog = true
            }) {
                Text("Pin")
            }
            Button(onClick = { 
                recordingController.saveRecording() 
                onNavigateBack()
            }) {
                Text("Save")
            }
            Button(onClick = { 
                recordingController.discardRecording() 
                onNavigateBack()
            }) {
                Text("Discard")
            }
        }
    }
}
