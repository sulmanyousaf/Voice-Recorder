package voicerecorder.applico.voice.recorder.core.media.recording.encoder

import java.io.File

interface AudioEncoder {
    fun start(outputFile: File, sampleRate: Int, bitRate: Int, append: Boolean = false)
    fun encode(pcmBuffer: ShortArray, bytesRead: Int)
    fun stop()
}
