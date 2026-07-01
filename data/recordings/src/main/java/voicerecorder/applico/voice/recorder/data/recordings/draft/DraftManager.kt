package voicerecorder.applico.voice.recorder.data.recordings.draft

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

import org.json.JSONObject

data class DraftMetadata(
    val recordingId: String,
    val format: String,
    val durationMs: Long,
    val sampleRate: Int,
    val bitRate: Int
)

interface DraftManager {
    suspend fun hasDraft(): Boolean
    suspend fun getDraftFile(): File?
    suspend fun getAmplitudesFile(): File?
    suspend fun saveMetadata(metadata: DraftMetadata)
    suspend fun getMetadata(): DraftMetadata?
    suspend fun discardDraft()
}

class DraftManagerImpl(private val context: Context) : DraftManager {
    private val METADATA_FILE_NAME = "draft_metadata.json"
    private val AMPLITUDES_FILE_NAME = "temp_amplitudes.bin"
    
    override suspend fun hasDraft(): Boolean = withContext(Dispatchers.IO) {
        getDraftFile() != null
    }

    override suspend fun getDraftFile(): File? = withContext(Dispatchers.IO) {
        val cacheDir = context.cacheDir
        if (!cacheDir.exists()) return@withContext null
        
        val files = cacheDir.listFiles() ?: return@withContext null
        
        // Find any file starting with "temp_recording"
        return@withContext files.firstOrNull { it.name.startsWith("temp_recording.") }
    }

    override suspend fun getAmplitudesFile(): File? = withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, AMPLITUDES_FILE_NAME)
        if (file.exists()) file else null
    }

    override suspend fun saveMetadata(metadata: DraftMetadata): Unit = withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, METADATA_FILE_NAME)
        val json = JSONObject().apply {
            put("recordingId", metadata.recordingId)
            put("format", metadata.format)
            put("durationMs", metadata.durationMs)
            put("sampleRate", metadata.sampleRate)
            put("bitRate", metadata.bitRate)
        }
        file.writeText(json.toString())
    }

    override suspend fun getMetadata(): DraftMetadata? = withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, METADATA_FILE_NAME)
        if (!file.exists()) return@withContext null
        
        try {
            val jsonText = file.readText()
            val json = JSONObject(jsonText)
            DraftMetadata(
                recordingId = json.getString("recordingId"),
                format = json.getString("format"),
                durationMs = json.getLong("durationMs"),
                sampleRate = json.getInt("sampleRate"),
                bitRate = json.getInt("bitRate")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun discardDraft(): Unit = withContext(Dispatchers.IO) {
        getDraftFile()?.delete()
        getAmplitudesFile()?.delete()
        val metadataFile = File(context.cacheDir, METADATA_FILE_NAME)
        if (metadataFile.exists()) {
            metadataFile.delete()
        }
    }
}
