package voicerecorder.applico.voice.recorder.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import voicerecorder.applico.voice.recorder.core.database.dao.RecordingDao
import voicerecorder.applico.voice.recorder.core.database.entity.RecordingEntity
import voicerecorder.applico.voice.recorder.core.database.entity.RecordingTagCrossRef
import voicerecorder.applico.voice.recorder.core.database.entity.TagEntity
import voicerecorder.applico.voice.recorder.core.database.entity.BookmarkEntity
import voicerecorder.applico.voice.recorder.core.database.dao.BookmarkDao

@Database(
    entities = [RecordingEntity::class, TagEntity::class, RecordingTagCrossRef::class, BookmarkEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordingDao(): RecordingDao
    abstract fun bookmarkDao(): BookmarkDao
}
