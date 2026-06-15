package voicerecorder.applico.voice.recorder.data.recordings.repository

import kotlinx.coroutines.flow.Flow
import voicerecorder.applico.voice.recorder.data.recordings.model.LocalRecording

interface RecordingRepository {
    fun getSavedRecordings(): Flow<List<LocalRecording>>
    fun scanDeviceAudio(): List<LocalRecording>
    suspend fun saveRecording(id: String, name: String, tempFilePath: String, durationMs: Long, sizeBytes: Long, format: String)
    suspend fun deleteRecording(recording: LocalRecording)
}
