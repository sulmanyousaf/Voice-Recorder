package voice.recorder.recordingvoice.cct.core.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.util.Calendar

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_DAILY = "voice_recorder_channel_daily"
        const val CHANNEL_PINNED = "voice_recorder_channel_pinned"

        const val NOTIFICATION_ID_DAILY = 2001
        const val NOTIFICATION_ID_PINNED = 2002
        const val NOTIFICATION_ID_KILL_APP = 2003

        const val KEY_OPEN_FROM = "KEY_OPEN_FROM"
        const val KEY_OPEN_TO = "KEY_OPEN_TO"

        const val OPEN_FROM_DEFAULT = 0
        const val OPEN_FROM_NOTIFY_DAILY = 1
        const val OPEN_FROM_NOTIFY_PINNED = 2
        const val OPEN_FROM_NOTIFY_KILL = 3
        const val OPEN_FROM_SHORTCUT = 4

        const val OPEN_TO_RECORD = 10
        const val OPEN_TO_LIBRARY = 11
        const val OPEN_TO_UNINSTALL = 99

        private const val CODE_REQUEST_DAILY_MORNING = 3001
        private const val CODE_REQUEST_DAILY_EVENING = 3002
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Morning and Evening engagement reminders"
                setShowBadge(true)
            }

            val pinnedChannel = NotificationChannel(
                CHANNEL_PINNED,
                "Quick Access",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Persistent widget to start recording quickly"
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(dailyChannel)
            notificationManager.createNotificationChannel(pinnedChannel)
        }
    }

    // --- Daily Reminders scheduling (AlarmManager) ---
    fun scheduleDailyReminders() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Schedule Morning Alarm: 09:00 AM
        scheduleAlarm(alarmManager, 9, 0, CODE_REQUEST_DAILY_MORNING)

        // Schedule Evening Alarm: 06:00 PM
        scheduleAlarm(alarmManager, 18, 0, CODE_REQUEST_DAILY_EVENING)
    }

    private fun scheduleAlarm(alarmManager: AlarmManager, hour: Int, minute: Int, requestCode: Int) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "voice.recorder.ACTION_SHOW_REMINDER"
            putExtra("EXTRA_REQUEST_CODE", requestCode)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        try {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Dismiss helper ---
    fun cancelNotification(id: Int) {
        try {
            NotificationManagerCompat.from(context).cancel(id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- App swiped / minimized reminder ---
    @SuppressLint("MissingPermission")
    fun showAppMinimizedNotification() {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            putExtra(KEY_OPEN_FROM, OPEN_FROM_NOTIFY_KILL)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            4001,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY)
            .setContentTitle(context.getString(R.string.notification_minimized_title))
            .setContentText(context.getString(R.string.notification_minimized_content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_KILL_APP, notification)
    }

    // --- Engagement / Daily alert ---
    @SuppressLint("MissingPermission")
    fun showEngagementNotification(title: String, content: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            putExtra(KEY_OPEN_FROM, OPEN_FROM_NOTIFY_DAILY)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            4002,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DAILY, notification)
    }

    // --- Pinned Persistent access widget ---
    @SuppressLint("MissingPermission")
    fun showPinnedNotification() {
        val bodyIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            putExtra(KEY_OPEN_FROM, OPEN_FROM_NOTIFY_PINNED)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val bodyPendingIntent = PendingIntent.getActivity(
            context,
            4003,
            bodyIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val recordIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            putExtra(KEY_OPEN_FROM, OPEN_FROM_NOTIFY_PINNED)
            putExtra(KEY_OPEN_TO, OPEN_TO_RECORD)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val recordPendingIntent = PendingIntent.getActivity(
            context,
            4004,
            recordIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PINNED)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle(context.getString(R.string.notification_pinned_title))
            .setContentText(context.getString(R.string.notification_pinned_content))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(bodyPendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSilent(true)
            .addAction(
                android.R.drawable.ic_media_play,
                context.getString(R.string.notification_pinned_action_new),
                recordPendingIntent
            )
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_PINNED, notification)
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }
}
