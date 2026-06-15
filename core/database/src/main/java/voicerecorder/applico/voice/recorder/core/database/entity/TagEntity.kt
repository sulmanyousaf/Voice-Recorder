package voicerecorder.applico.voice.recorder.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val name: String,
    val colorHex: String
)
