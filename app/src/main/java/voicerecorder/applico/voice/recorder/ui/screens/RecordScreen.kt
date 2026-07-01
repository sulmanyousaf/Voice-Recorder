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

@Composable
fun RecordScreen(
    recordingController: RecordingController,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentAmplitude by recordingController.amplitudeFlow.collectAsState(initial = 0f)
    val amplitudes = remember { mutableStateListOf<Float>() }

    LaunchedEffect(currentAmplitude) {
        amplitudes.add(currentAmplitude)
        if (amplitudes.size > 200) {
            amplitudes.removeAt(0)
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
            modifier = Modifier.fillMaxWidth().height(150.dp)
        )
        
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
            Button(onClick = { recordingController.pinRecording() }) {
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
