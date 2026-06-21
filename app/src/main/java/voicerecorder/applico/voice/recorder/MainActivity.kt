package voicerecorder.applico.voice.recorder

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import org.koin.android.ext.android.inject
import voicerecorder.applico.voice.recorder.core.common.util.createLocalizedContext
import voicerecorder.applico.voice.recorder.core.designsystem.theme.VoiceRecorderTheme
import voicerecorder.applico.voice.recorder.core.datastore.AudioSettingsDataStore
import voicerecorder.applico.voice.recorder.core.datastore.UserSettings
import voicerecorder.applico.voice.recorder.core.notifications.NotificationHelper
import voicerecorder.applico.voice.recorder.core.shortcuts.VoiceRecorderShortcutManager
import voicerecorder.applico.voice.recorder.core.overlay.OverlayManager
import voicerecorder.applico.voice.recorder.core.overlay.SnackbarManager
import voicerecorder.applico.voice.recorder.core.overlay.model.Overlay
import voicerecorder.applico.voice.recorder.core.permissions.rememberMicrophonePermissionHandler
import voicerecorder.applico.voice.recorder.core.designsystem.components.PermissionRationaleDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.res.stringResource
import voicerecorder.applico.voice.recorder.core.designsystem.theme.LocalDimensions
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {

    private val settingsDataStore: AudioSettingsDataStore by inject()
    private val notificationHelper: NotificationHelper by inject()
    private val shortcutManager: VoiceRecorderShortcutManager by inject()
    private val overlayManager: OverlayManager by inject()
    private val snackbarManager: SnackbarManager by inject()

    override fun attachBaseContext(newBase: Context) {
        val settings = AudioSettingsDataStore(newBase)
        val langCode = settings.getLanguageBlocking()
        val localizedContext = createLocalizedContext(newBase, langCode)
        super.attachBaseContext(localizedContext)
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val initialLang = remember { settingsDataStore.getLanguageBlocking() }
            val userSettings by settingsDataStore.settingsFlow.collectAsState(
                initial = UserSettings(languageCode = initialLang)
            )
            val localizedContext = remember(userSettings.languageCode) {
                createLocalizedContext(this@MainActivity, userSettings.languageCode).also {
                    shortcutManager.updateShortcuts(it)
                }
            }

            CompositionLocalProvider(LocalContext provides localizedContext) {
                val windowSizeClass = calculateWindowSizeClass(this@MainActivity)
                VoiceRecorderTheme(windowSizeClass = windowSizeClass) {
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
                            Button(onClick = { permissionHandler() }, modifier = Modifier.padding(top = LocalDimensions.current.spacingMedium)) {
                                Text(stringResource(id = R.string.request_microphone_permission))
                            }
                            Button(onClick = { snackbarManager.showMessage(R.string.app_name) }, modifier = Modifier.padding(top = LocalDimensions.current.spacingMedium)) {
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
            }
        }
    }

    override fun onResume() {
        super.onResume()
        notificationHelper.cancelNotification(NotificationHelper.NOTIFICATION_ID_KILL_APP)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
