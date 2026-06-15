package voicerecorder.applico.voice.recorder.core.media.storage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

interface RecordingStorage {
    fun getTempFile(format: String): Result<File>
    fun saveToPublicFolder(tempFile: File, displayName: String, format: String): Result<Uri>
}

class AndroidRecordingStorage(private val context: Context) : RecordingStorage {

    override fun getTempFile(format: String): Result<File> = runCatching {
        val file = File(context.cacheDir, "temp_recording.${format.lowercase()}")
        if (!file.exists()) {
            file.createNewFile() // This will throw IOException if storage is completely full
        }
        file
    }

    override fun saveToPublicFolder(tempFile: File, displayName: String, format: String): Result<Uri> = runCatching {
        val ext = format.lowercase()
        val recordingsDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Environment.DIRECTORY_RECORDINGS
        } else {
            "Recordings"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, "$displayName.$ext")
                put(MediaStore.Audio.Media.MIME_TYPE, getMimeType(ext))
                put(MediaStore.Audio.Media.RELATIVE_PATH, "$recordingsDir/VoiceRecorder")
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }
            
            val resolver = context.contentResolver
            val targetUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw java.io.IOException("Failed to create MediaStore record")
                
            resolver.openOutputStream(targetUri)?.use { out ->
                tempFile.inputStream().use { input ->
                    input.copyTo(out)
                }
            } ?: throw java.io.IOException("Failed to open output stream")
            
            contentValues.clear()
            contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
            resolver.update(targetUri, contentValues, null, null)
            tempFile.delete()
            targetUri
        } else {
            @Suppress("DEPRECATION")
            val publicDir = File(Environment.getExternalStoragePublicDirectory(recordingsDir), "VoiceRecorder")
            if (!publicDir.exists() && !publicDir.mkdirs()) {
                 throw java.io.IOException("Failed to create public directory")
            }
            val targetFile = File(publicDir, "$displayName.$ext")
            tempFile.copyTo(targetFile, overwrite = true)
            tempFile.delete()
            Uri.fromFile(targetFile)
        }
    }

    private fun getMimeType(extension: String): String {
        return when (extension) {
            "wav" -> "audio/wav"
            "mp3" -> "audio/mp3"
            "amr" -> "audio/amr"
            else -> "audio/mp4"
        }
    }
}
