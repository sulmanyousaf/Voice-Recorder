package voicerecorder.applico.voice.recorder.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey val id: String,
    val name: String,
    val uriString: String,
    val filePath: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val format: String,
    val createdAtMs: Long
)
