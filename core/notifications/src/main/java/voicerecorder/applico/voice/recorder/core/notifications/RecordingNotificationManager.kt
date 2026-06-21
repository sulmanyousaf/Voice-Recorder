package voicerecorder.applico.voice.recorder.core.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class RecordingNotificationManager(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        const val CHANNEL_ID = "recording_channel"
        const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_service_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.channel_service_desc)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun buildNotification(durationText: String): Notification {
        val customLayout = RemoteViews(context.packageName, R.layout.notification_recording).apply {
            setTextViewText(R.id.text_duration, durationText)
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.presence_video_busy)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(customLayout)
            .setColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    fun updateNotification(durationText: String) {
        val notification = buildNotification(durationText)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
