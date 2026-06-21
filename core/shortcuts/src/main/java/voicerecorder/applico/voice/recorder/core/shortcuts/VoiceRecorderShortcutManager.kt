package voicerecorder.applico.voice.recorder.core.shortcuts

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import voicerecorder.applico.voice.recorder.core.notifications.NotificationHelper

class VoiceRecorderShortcutManager(
    private val applicationContext: Context,
    private val targetIntentFactory: () -> Intent
) {

    companion object {
        const val SHORTCUT_UNINSTALL = "shortcut_uninstall"
    }

    fun updateShortcuts(localizedContext: Context) {
        try {
            val uninstallShortcut = ShortcutInfoCompat.Builder(applicationContext, SHORTCUT_UNINSTALL)
                .setShortLabel("Uninstall")
                .setLongLabel("Uninstall App")
                .setIntent(
                    targetIntentFactory().apply {
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
