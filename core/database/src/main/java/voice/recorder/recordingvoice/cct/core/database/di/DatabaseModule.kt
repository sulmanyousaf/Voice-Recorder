package voice.recorder.recordingvoice.cct.core.database.di

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import voice.recorder.recordingvoice.cct.core.database.AppDatabase

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "voice_recorder_app_db"
        ).build()
    }
    single { get<AppDatabase>().recordingDao() }
}
