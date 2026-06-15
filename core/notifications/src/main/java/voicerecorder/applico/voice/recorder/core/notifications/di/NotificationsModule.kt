package voicerecorder.applico.voice.recorder.core.notifications.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import voicerecorder.applico.voice.recorder.core.notifications.NotificationHelper
import voicerecorder.applico.voice.recorder.core.notifications.RecordingNotificationManager

val notificationsModule = module {
    single { RecordingNotificationManager(androidContext()) }
    single { NotificationHelper(androidContext()) }
}
