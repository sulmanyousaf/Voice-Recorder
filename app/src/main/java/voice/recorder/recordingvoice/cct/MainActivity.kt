package voice.recorder.recordingvoice.cct

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
import voice.recorder.recordingvoice.cct.core.common.util.createLocalizedContext
import voice.recorder.recordingvoice.cct.core.designsystem.theme.VoiceRecorderTheme
import voice.recorder.recordingvoice.cct.core.datastore.AudioSettingsDataStore
import voice.recorder.recordingvoice.cct.core.datastore.UserSettings
import voice.recorder.recordingvoice.cct.core.notifications.NotificationHelper
import voice.recorder.recordingvoice.cct.shortcut.VoiceRecorderShortcutManager

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
                VoiceRecorderTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Greeting(
                            name = "Android",
                            modifier = Modifier.padding(innerPadding)
                        )
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VoiceRecorderTheme {
        Greeting("Android")
    }
}
