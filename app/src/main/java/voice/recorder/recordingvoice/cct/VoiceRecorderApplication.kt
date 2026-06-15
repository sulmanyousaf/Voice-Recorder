package voice.recorder.recordingvoice.cct

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module
import voice.recorder.recordingvoice.cct.core.common.di.commonModule
import voice.recorder.recordingvoice.cct.core.database.di.databaseModule
import voice.recorder.recordingvoice.cct.core.datastore.di.dataStoreModule
import voice.recorder.recordingvoice.cct.core.media.di.mediaModule
import voice.recorder.recordingvoice.cct.core.notifications.di.notificationsModule
import voice.recorder.recordingvoice.cct.core.overlay.di.overlayModule
import voice.recorder.recordingvoice.cct.data.recordings.di.recordingsDataModule
import voice.recorder.recordingvoice.cct.shortcut.VoiceRecorderShortcutManager

val appModule = module {
    single { VoiceRecorderShortcutManager(androidContext()) }
}

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
                appModule
            )
        }
        val notificationHelper: voice.recorder.recordingvoice.cct.core.notifications.NotificationHelper = koinApp.koin.get()
        notificationHelper.scheduleDailyReminders()
    }
}
