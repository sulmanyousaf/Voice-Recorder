package voicerecorder.applico.voice.recorder.data.recordings.scanner

import android.content.Context
import android.provider.MediaStore
import voicerecorder.applico.voice.recorder.data.recordings.model.LocalRecording

class MediaStoreScanner(private val context: Context) {

    fun scanAllDeviceAudio(): List<LocalRecording> {
        val audioList = mutableListOf<LocalRecording>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Audio.Media.MIME_TYPE} LIKE ? OR " +
                "${MediaStore.Audio.Media.MIME_TYPE} LIKE ? OR " +
                "${MediaStore.Audio.Media.MIME_TYPE} LIKE ? OR " +
                "${MediaStore.Audio.Media.MIME_TYPE} LIKE ? OR " +
                "${MediaStore.Audio.Media.DATA} LIKE ?"
                
        val selectionArgs = arrayOf(
            "audio/mpeg",
            "audio/x-wav",
            "audio/wav",
            "audio/x-m4a",
            "%.aac"
        )

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        )

        cursor?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (c.moveToNext()) {
                val id = c.getLong(idCol).toString()
                val name = c.getString(nameCol) ?: "Unknown Audio"
                val path = c.getString(dataCol) ?: ""
                val duration = c.getLong(durationCol)
                val size = c.getLong(sizeCol)
                val date = c.getLong(dateCol) * 1000L

                val format = when {
                    path.endsWith(".mp3", true) -> "MP3"
                    path.endsWith(".wav", true) -> "WAV"
                    path.endsWith(".m4a", true) -> "M4A"
                    path.endsWith(".aac", true) -> "AAC"
                    else -> "M4A"
                }

                audioList.add(
                    LocalRecording(
                        id = id,
                        name = name,
                        uriString = "${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}/$id",
                        filePath = path,
                        durationMs = duration,
                        sizeBytes = size,
                        format = format,
                        createdAtMs = date
                    )
                )
            }
        }
        return audioList
    }
}
