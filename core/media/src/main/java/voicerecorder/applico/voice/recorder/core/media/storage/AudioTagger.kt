package voicerecorder.applico.voice.recorder.core.media.storage

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.id3.ID3v24Tag
import org.jaudiotagger.tag.id3.ID3v24Frame
import org.jaudiotagger.tag.id3.framebody.FrameBodyCHAP
import org.jaudiotagger.tag.id3.framebody.FrameBodyCTOC
import org.jaudiotagger.tag.id3.framebody.FrameBodyTIT2
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
        
        val ext = audioFile.extension.lowercase()
        
        try {
            when (ext) {
                "mp3", "wav", "aac", "m4a", "mp4" -> injectId3v2Chapters(audioFile, pins)
                else -> injectId3v2Chapters(audioFile, pins)
            }
        } catch (e: Exception) {
            Log.e("AudioTagger", "Error injecting chapters for $ext", e)
            // Fallback to basic JSON comment if advanced chapters fail
            injectJsonFallback(audioFile, pins)
        }
    }
    
    private fun injectId3v2Chapters(file: File, pins: List<AudioPin>) {
        val audioObj = AudioFileIO.read(file)
        val tag = audioObj.tag ?: ID3v24Tag()
        
        if (tag is org.jaudiotagger.tag.id3.AbstractID3v2Tag) {
            val elementIds = mutableListOf<String>()
            
            pins.forEachIndexed { index, pin ->
                val chId = "chp$index"
                elementIds.add(chId)
                
                // End time is usually next pin or just start + some duration. We'll set end time same as start for a point marker.
                val chapBody = FrameBodyCHAP(chId, pin.timeMs.toInt(), pin.timeMs.toInt(), -1, -1)
                
                // Add title to chapter
                val titleFrame = ID3v24Frame("TIT2")
                val titleNote = if (pin.note.isNotBlank()) pin.note else "Pin ${index + 1}"
                titleFrame.body = FrameBodyTIT2(0.toByte(), titleNote)
                
                val chapFrame = ID3v24Frame("CHAP")
                chapFrame.body = chapBody
                // Add the sub-frame (jaudiotagger structure)
                // Note: older versions might not support addSubFrame easily without hack, so we'll just write the JSON fallback as well
                
                tag.addFrame(chapFrame)
            }
            
            audioObj.tag = tag
            audioObj.commit()
            
            // Also write JSON fallback so our app can read it reliably
            injectJsonFallback(file, pins)
        } else {
            injectJsonFallback(file, pins)
        }
    }
    
    private fun injectJsonFallback(audioFile: File, pins: List<AudioPin>) {
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
            
            tag.setField(FieldKey.COMMENT, "VoiceRecorderPins:${jsonArray}")
            audioObj.commit()
        } catch (e: Exception) {
            Log.e("AudioTagger", "Error injecting JSON fallback", e)
        }
    }
}
