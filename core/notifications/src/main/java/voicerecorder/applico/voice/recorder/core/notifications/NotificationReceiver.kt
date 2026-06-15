package voicerecorder.applico.voice.recorder.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.java.KoinJavaComponent.getKoin

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        
        try {
            val helper: NotificationHelper = getKoin().get()
            
            when (action) {
                Intent.ACTION_BOOT_COMPLETED,
                "android.intent.action.QUICKBOOT_POWERON" -> {
                    helper.scheduleDailyReminders()
                }
                "voicerecorder.applico.voice.recorder.ACTION_SHOW_REMINDER" -> {
                    val requestCode = intent.getIntExtra("EXTRA_REQUEST_CODE", -1)
                    if (requestCode == 3001) {
                        helper.showEngagementNotification(
                            context.getString(R.string.notification_morning_title),
                            context.getString(R.string.notification_morning_content)
                        )
                    } else if (requestCode == 3002) {
                        helper.showEngagementNotification(
                            context.getString(R.string.notification_evening_title),
                            context.getString(R.string.notification_evening_content)
                        )
                    }
                    helper.scheduleDailyReminders()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
