package voicerecorder.applico.voice.recorder.core.designsystem.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun DraftRecoveryDialog(
    onResume: () -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Must choose an action explicitly */ },
        title = { Text("Draft Found") },
        text = { Text("You have an unsaved recording from a previous session. Would you like to resume it, save it immediately, or discard it?") },
        confirmButton = {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onSave) {
                    Text("Save")
                }
                TextButton(onClick = onResume) {
                    Text("Resume")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscard) {
                Text("Discard")
            }
        }
    )
}
