package voicerecorder.applico.voice.recorder

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import voicerecorder.applico.voice.recorder.core.common.di.commonModule
import voicerecorder.applico.voice.recorder.core.database.di.databaseModule
import voicerecorder.applico.voice.recorder.core.datastore.di.dataStoreModule
import voicerecorder.applico.voice.recorder.core.media.di.mediaModule
import voicerecorder.applico.voice.recorder.core.notifications.di.notificationsModule
import voicerecorder.applico.voice.recorder.core.overlay.di.overlayModule
import voicerecorder.applico.voice.recorder.data.recordings.di.recordingsDataModule
import voicerecorder.applico.voice.recorder.feature.recordings.di.featureRecordingsModule
import voicerecorder.applico.voice.recorder.di.appModule

class VoiceRecorderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val koinApp = startKoin {
            androidLogger()
            androidContext(this@VoiceRecorderApplication)
            modules(
                commonModule,
                databaseModule,
                dataStoreModule,
                mediaModule,
                recordingsDataModule,
                notificationsModule,
                overlayModule,
                featureRecordingsModule,
                appModule
            )
        }
        val notificationHelper: voicerecorder.applico.voice.recorder.core.notifications.NotificationHelper = koinApp.koin.get()
        notificationHelper.scheduleDailyReminders()
    }
}
