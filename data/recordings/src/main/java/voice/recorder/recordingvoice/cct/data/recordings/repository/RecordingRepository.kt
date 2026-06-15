package voice.recorder.recordingvoice.cct.data.recordings.repository

import kotlinx.coroutines.flow.Flow
import voice.recorder.recordingvoice.cct.data.recordings.model.LocalRecording

interface RecordingRepository {
    fun getSavedRecordings(): Flow<List<LocalRecording>>
    fun scanDeviceAudio(): List<LocalRecording>
    suspend fun saveRecording(id: String, name: String, tempFilePath: String, durationMs: Long, sizeBytes: Long, format: String)
    suspend fun deleteRecording(recording: LocalRecording)
}
