package voicerecorder.applico.voice.recorder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import voicerecorder.applico.voice.recorder.core.media.playback.PlaybackEngine
import voicerecorder.applico.voice.recorder.feature.recordings.viewmodel.RecordingsViewModel

@Composable
fun PlaybackScreen(
    recordingId: String,
    viewModel: RecordingsViewModel,
    playbackEngine: PlaybackEngine,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recordings by viewModel.recordings.collectAsState()
    val recording = recordings.find { it.id == recordingId }

    val isPlaying by playbackEngine.isPlaying.collectAsState()
    val currentPosition by playbackEngine.currentPosition.collectAsState()
    val duration by playbackEngine.duration.collectAsState()

    val bookmarks by remember(recordingId) { viewModel.getBookmarksFlow(recordingId) }.collectAsState(initial = emptyList())

    DisposableEffect(recordingId) {
        if (recording != null) {
            playbackEngine.play(recording.id, recording.uriString)
        }
        onDispose {
            playbackEngine.stop()
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (recording == null) {
            Text("Recording not found.")
            Button(onClick = onNavigateBack, modifier = Modifier.padding(top = 16.dp)) {
                Text("Back")
            }
        } else {
            Text(recording.name, style = MaterialTheme.typography.headlineMedium)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            val progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${currentPosition / 1000}s")
                Text("${duration / 1000}s")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (isPlaying) {
                    Button(onClick = { playbackEngine.pause() }) {
                        Text("Pause")
                    }
                } else {
                    Button(onClick = { playbackEngine.resume() }) {
                        Text("Play")
                    }
                }
                
                Button(onClick = { playbackEngine.seekTo(0) }) {
                    Text("Restart")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onNavigateBack) {
                Text("Close Player")
            }

            if (bookmarks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Pins & Notes", style = MaterialTheme.typography.titleMedium)
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(bookmarks.size) { index ->
                        val bookmark = bookmarks[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = { playbackEngine.seekTo(bookmark.timestampMs) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Time: ${bookmark.timestampMs / 1000}s", style = MaterialTheme.typography.bodySmall)
                                Text(bookmark.noteText, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
