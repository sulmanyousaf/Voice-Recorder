package voice.recorder.recordingvoice.cct.core.media.recording.encoder

import java.io.File

interface AudioEncoder {
    fun start(outputFile: File, sampleRate: Int, bitRate: Int)
    fun encode(pcmBuffer: ShortArray, bytesRead: Int)
    fun stop()
}
