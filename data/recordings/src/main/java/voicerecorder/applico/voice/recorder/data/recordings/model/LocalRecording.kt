package voicerecorder.applico.voice.recorder.data.recordings.model

data class LocalRecording(
    val id: String,
    val name: String,
    val uriString: String,
    val filePath: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val format: String,
    val createdAtMs: Long
)
