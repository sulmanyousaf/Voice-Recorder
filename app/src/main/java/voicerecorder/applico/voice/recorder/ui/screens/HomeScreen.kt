package voicerecorder.applico.voice.recorder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import voicerecorder.applico.voice.recorder.Greeting
import voicerecorder.applico.voice.recorder.R
import voicerecorder.applico.voice.recorder.core.designsystem.theme.LocalDimensions

@Composable
fun HomeScreen(
    onNavigateToRecord: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateToPlayback: () -> Unit,
    onTestPermissions: () -> Unit,
    onTestHardware: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Greeting(name = "Voice Recorder")
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onNavigateToRecord,
            modifier = Modifier.padding(top = LocalDimensions.current.spacingMedium)
        ) {
            Text("Go to Recording Screen")
        }
        
        Button(
            onClick = onNavigateToList,
            modifier = Modifier.padding(top = LocalDimensions.current.spacingMedium)
        ) {
            Text("View Saved Recordings")
        }
        
        Button(
            onClick = onNavigateToPlayback,
            modifier = Modifier.padding(top = LocalDimensions.current.spacingMedium)
        ) {
            Text("Test Playback Screen")
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onTestPermissions,
            modifier = Modifier.padding(top = LocalDimensions.current.spacingMedium)
        ) {
            Text(stringResource(id = R.string.request_microphone_permission))
        }

        Button(
            onClick = onTestHardware,
            modifier = Modifier.padding(top = LocalDimensions.current.spacingMedium)
        ) {
            Text("Check Audio Hardware")
        }
    }
}
