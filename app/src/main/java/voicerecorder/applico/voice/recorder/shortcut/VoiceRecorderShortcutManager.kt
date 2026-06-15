package voicerecorder.applico.voice.recorder.shortcut

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import voicerecorder.applico.voice.recorder.MainActivity
import voicerecorder.applico.voice.recorder.core.notifications.NotificationHelper
import voicerecorder.applico.voice.recorder.core.notifications.R

class VoiceRecorderShortcutManager(private val applicationContext: Context) {

    companion object {
        const val SHORTCUT_UNINSTALL = "shortcut_uninstall"
    }

    fun updateShortcuts(localizedContext: Context) {
        try {
            val uninstallShortcut = ShortcutInfoCompat.Builder(applicationContext, SHORTCUT_UNINSTALL)
                .setShortLabel(localizedContext.getString(R.string.shortcut_uninstall_label))
                .setLongLabel("Uninstall App")
                .setIntent(
                    Intent(applicationContext, MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra(NotificationHelper.KEY_OPEN_TO, NotificationHelper.OPEN_TO_UNINSTALL)
                        putExtra(NotificationHelper.KEY_OPEN_FROM, NotificationHelper.OPEN_FROM_SHORTCUT)
                    }
                )
                .setIcon(IconCompat.createWithResource(applicationContext, android.R.drawable.ic_menu_delete))
                .build()

            ShortcutManagerCompat.setDynamicShortcuts(applicationContext, listOf(uninstallShortcut))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
