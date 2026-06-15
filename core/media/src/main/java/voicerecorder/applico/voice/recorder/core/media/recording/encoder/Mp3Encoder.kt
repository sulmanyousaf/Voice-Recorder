package voicerecorder.applico.voice.recorder.core.media.recording.encoder

import java.io.File
import java.io.FileOutputStream

class Mp3Encoder : AudioEncoder {
    private val lame = LameEncoder()
    private var outputStream: FileOutputStream? = null
    private var mp3Buffer: ByteArray? = null

    override fun start(outputFile: File, sampleRate: Int, bitRate: Int) {
        lame.init(
            inSampleRate = sampleRate,
            outChannel = 1,
            outSampleRate = sampleRate,
            outBitrate = bitRate / 1000,
            quality = 5
        )
        outputStream = FileOutputStream(outputFile)
        mp3Buffer = ByteArray((7200 + 1.25 * 4096).toInt())
    }

    override fun encode(pcmBuffer: ShortArray, bytesRead: Int) {
        val buffer = mp3Buffer ?: return
        val size = lame.encode(pcmBuffer, pcmBuffer, bytesRead, buffer)
        if (size > 0) {
            outputStream?.write(buffer, 0, size)
        }
    }

    override fun stop() {
        val buffer = mp3Buffer ?: return
        val size = lame.flush(buffer)
        if (size > 0) {
            outputStream?.write(buffer, 0, size)
        }
        lame.close()
        outputStream?.close()
        outputStream = null
        mp3Buffer = null
    }
}
