package voicerecorder.applico.voice.recorder.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import voicerecorder.applico.voice.recorder.feature.recordings.viewmodel.RecordingsViewModel

@Composable
fun RecordingsListScreen(
    viewModel: RecordingsViewModel,
    onNavigateToPlayback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val recordings by viewModel.recordings.collectAsState()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Saved Recordings", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (recordings.isEmpty()) {
            Text("No recordings found.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(recordings) { recording ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPlayback(recording.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(recording.name, style = MaterialTheme.typography.titleMedium)
                            Text("Duration: ${recording.durationMs / 1000}s", style = MaterialTheme.typography.bodyMedium)
                            Text("Format: ${recording.format}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
