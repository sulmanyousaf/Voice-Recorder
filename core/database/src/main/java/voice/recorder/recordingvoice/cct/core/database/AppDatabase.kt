package voice.recorder.recordingvoice.cct.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import voice.recorder.recordingvoice.cct.core.database.dao.RecordingDao
import voice.recorder.recordingvoice.cct.core.database.entity.RecordingEntity
import voice.recorder.recordingvoice.cct.core.database.entity.RecordingTagCrossRef
import voice.recorder.recordingvoice.cct.core.database.entity.TagEntity

@Database(
    entities = [RecordingEntity::class, TagEntity::class, RecordingTagCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordingDao(): RecordingDao
}
