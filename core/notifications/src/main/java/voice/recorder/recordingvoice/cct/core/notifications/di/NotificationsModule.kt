package voice.recorder.recordingvoice.cct.core.notifications.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import voice.recorder.recordingvoice.cct.core.notifications.NotificationHelper
import voice.recorder.recordingvoice.cct.core.notifications.RecordingNotificationManager

val notificationsModule = module {
    single { RecordingNotificationManager(androidContext()) }
    single { NotificationHelper(androidContext()) }
}
