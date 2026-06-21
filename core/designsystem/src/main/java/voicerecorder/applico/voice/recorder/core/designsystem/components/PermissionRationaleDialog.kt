package voicerecorder.applico.voice.recorder.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import voicerecorder.applico.voice.recorder.core.designsystem.R
import voicerecorder.applico.voice.recorder.core.designsystem.theme.LocalDimensions

@Composable
fun PermissionRationaleDialog(
    title: String,
    description: String,
    iconRes: Int?,
    onProceed: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
        modifier = modifier,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (iconRes != null) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(LocalDimensions.current.iconSizeLarge),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(LocalDimensions.current.spacingMedium))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = onProceed) {
                Text(stringResource(id = R.string.action_allow))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.action_not_now))
            }
        },
        shape = RoundedCornerShape(LocalDimensions.current.dialogCornerRadius),
        tonalElevation = LocalDimensions.current.dialogElevation
    )
}
