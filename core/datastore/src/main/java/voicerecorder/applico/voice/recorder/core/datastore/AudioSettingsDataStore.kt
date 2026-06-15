package voicerecorder.applico.voice.recorder.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore("audio_settings")

data class UserSettings(
    val format: String = "M4A", // WAV, AAC, M4A, MP3
    val sampleRate: Int = 44100,
    val bitRate: Int = 128000,
    val enableNotifications: Boolean = true,
    val pauseForCall: Boolean = true,
    val languageCode: String = "en"
)

class AudioSettingsDataStore(private val context: Context) {
    private object Keys {
        val FORMAT = stringPreferencesKey("format")
        val SAMPLE_RATE = intPreferencesKey("sample_rate")
        val BIT_RATE = intPreferencesKey("bit_rate")
        val ENABLE_NOTIFICATIONS = booleanPreferencesKey("enable_notifications")
        val PAUSE_FOR_CALL = booleanPreferencesKey("pause_for_call")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
    }

    fun getLanguageBlocking(): String {
        return try {
            runBlocking {
                context.dataStore.data.first()[Keys.LANGUAGE_CODE] ?: "en"
            }
        } catch (e: Exception) {
            "en"
        }
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            format = prefs[Keys.FORMAT] ?: "M4A",
            sampleRate = prefs[Keys.SAMPLE_RATE] ?: 44100,
            bitRate = prefs[Keys.BIT_RATE] ?: 128000,
            enableNotifications = prefs[Keys.ENABLE_NOTIFICATIONS] ?: true,
            pauseForCall = prefs[Keys.PAUSE_FOR_CALL] ?: true,
            languageCode = prefs[Keys.LANGUAGE_CODE] ?: "en"
        )
    }

    suspend fun updateFormat(format: String) = context.dataStore.edit { it[Keys.FORMAT] = format }
    suspend fun updateSampleRate(rate: Int) = context.dataStore.edit { it[Keys.SAMPLE_RATE] = rate }
    suspend fun updateBitRate(bitRate: Int) = context.dataStore.edit { it[Keys.BIT_RATE] = bitRate }
    suspend fun updateNotifications(enabled: Boolean) = context.dataStore.edit { it[Keys.ENABLE_NOTIFICATIONS] = enabled }
    suspend fun updatePauseForCall(enabled: Boolean) = context.dataStore.edit { it[Keys.PAUSE_FOR_CALL] = enabled }
    suspend fun updateLanguage(code: String) = context.dataStore.edit { it[Keys.LANGUAGE_CODE] = code }
}
