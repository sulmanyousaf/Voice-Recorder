package voice.recorder.recordingvoice.cct.data.recordings.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import voice.recorder.recordingvoice.cct.core.database.dao.RecordingDao
import voice.recorder.recordingvoice.cct.core.database.entity.RecordingEntity
import voice.recorder.recordingvoice.cct.core.media.storage.RecordingStorage
import voice.recorder.recordingvoice.cct.data.recordings.model.LocalRecording
import voice.recorder.recordingvoice.cct.data.recordings.scanner.MediaStoreScanner
import java.io.File

class RecordingRepositoryImpl(
    private val recordingDao: RecordingDao,
    private val storage: RecordingStorage,
    private val scanner: MediaStoreScanner
) : RecordingRepository {

    override fun getSavedRecordings(): Flow<List<LocalRecording>> {
        return recordingDao.getAllRecordings().map { entities ->
            entities.map {
                LocalRecording(
                    id = it.id,
                    name = it.name,
                    uriString = it.uriString,
                    filePath = it.filePath,
                    durationMs = it.durationMs,
                    sizeBytes = it.sizeBytes,
                    format = it.format,
                    createdAtMs = it.createdAtMs
                )
            }
        }
    }

    override fun scanDeviceAudio(): List<LocalRecording> = scanner.scanAllDeviceAudio()

    override suspend fun saveRecording(
        id: String,
        name: String,
        tempFilePath: String,
        durationMs: Long,
        sizeBytes: Long,
        format: String
    ) {
        val tempFile = File(tempFilePath)
        val publicUri = storage.saveToPublicFolder(tempFile, name, format)
        
        publicUri?.let { uri ->
            val entity = RecordingEntity(
                id = id,
                name = name,
                uriString = uri.toString(),
                filePath = tempFilePath,
                durationMs = durationMs,
                sizeBytes = sizeBytes,
                format = format,
                createdAtMs = System.currentTimeMillis()
            )
            recordingDao.insertRecording(entity)
        }
    }

    override suspend fun deleteRecording(recording: LocalRecording) {
        recordingDao.deleteRecording(
            RecordingEntity(
                id = recording.id,
                name = recording.name,
                uriString = recording.uriString,
                filePath = recording.filePath,
                durationMs = recording.durationMs,
                sizeBytes = recording.sizeBytes,
                format = recording.format,
                createdAtMs = recording.createdAtMs
            )
        )
    }
}
