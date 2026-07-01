package voicerecorder.applico.voice.recorder.core.database.di

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import voicerecorder.applico.voice.recorder.core.database.AppDatabase

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "voice_recorder_app_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    single { get<AppDatabase>().recordingDao() }
    single { get<AppDatabase>().bookmarkDao() }
}
