package voicerecorder.applico.voice.recorder.core.media.storage

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import android.util.Log

data class AudioPin(val timeMs: Long, val note: String)

interface AudioTagger {
    fun injectPins(audioFile: File, pins: List<AudioPin>)
}

class AudioTaggerImpl : AudioTagger {
    override fun injectPins(audioFile: File, pins: List<AudioPin>) {
        if (pins.isEmpty() || !audioFile.exists()) return
        try {
            val audioObj = AudioFileIO.read(audioFile)
            val tag = audioObj.tagOrCreateAndSetDefault
            
            val jsonArray = JSONArray()
            pins.forEach { pin ->
                val obj = JSONObject()
                obj.put("timeMs", pin.timeMs)
                obj.put("note", pin.note)
                jsonArray.put(obj)
            }
            
            val jsonString = jsonArray.toString()
            tag.setField(FieldKey.COMMENT, "VoiceRecorderPins:$jsonString")
            
            audioObj.commit()
        } catch (e: Exception) {
            Log.e("AudioTagger", "Error injecting pins into audio file", e)
        }
    }
}
