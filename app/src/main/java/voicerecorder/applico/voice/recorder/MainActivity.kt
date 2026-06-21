package voicerecorder.applico.voice.recorder

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.koin.android.ext.android.inject
import voicerecorder.applico.voice.recorder.core.common.util.createLocalizedContext
import voicerecorder.applico.voice.recorder.core.designsystem.theme.VoiceRecorderTheme
import voicerecorder.applico.voice.recorder.core.datastore.AudioSettingsDataStore
import voicerecorder.applico.voice.recorder.core.datastore.UserSettings
import voicerecorder.applico.voice.recorder.core.notifications.NotificationHelper
import voicerecorder.applico.voice.recorder.core.shortcuts.VoiceRecorderShortcutManager
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {

    private val settingsDataStore: AudioSettingsDataStore by inject()
    private val notificationHelper: NotificationHelper by inject()
    private val shortcutManager: VoiceRecorderShortcutManager by inject()

    override fun attachBaseContext(newBase: Context) {
        val settings = AudioSettingsDataStore(newBase)
        val langCode = settings.getLanguageBlocking()
        val localizedContext = createLocalizedContext(newBase, langCode)
        super.attachBaseContext(localizedContext)
    }

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
                    VoiceRecorderApp(windowSizeClass = windowSizeClass)
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

