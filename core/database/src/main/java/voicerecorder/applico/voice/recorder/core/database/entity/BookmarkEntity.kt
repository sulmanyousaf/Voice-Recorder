package voicerecorder.applico.voice.recorder.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val recordingId: String,
    val timestampMs: Long,
    val noteText: String = ""
)
