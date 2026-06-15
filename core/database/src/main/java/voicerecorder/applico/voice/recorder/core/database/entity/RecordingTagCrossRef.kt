package voicerecorder.applico.voice.recorder.core.database.entity

import androidx.room.Entity

@Entity(tableName = "recording_tag_cross_ref", primaryKeys = ["id", "name"])
data class RecordingTagCrossRef(
    val id: String,
    val name: String
)
