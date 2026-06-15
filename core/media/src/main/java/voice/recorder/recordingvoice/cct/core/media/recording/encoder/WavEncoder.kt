package voice.recorder.recordingvoice.cct.core.media.recording.encoder

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WavEncoder : AudioEncoder {
    private var randomAccessFile: RandomAccessFile? = null
    private var totalAudioLen: Long = 0
    private var totalDataLen: Long = 0
    private var sampleRate: Int = 44100

    override fun start(outputFile: File, sampleRate: Int, bitRate: Int) {
        this.sampleRate = sampleRate
        totalAudioLen = 0
        totalDataLen = 0
        randomAccessFile = RandomAccessFile(outputFile, "rw")
        randomAccessFile?.write(ByteArray(44))
    }

    override fun encode(pcmBuffer: ShortArray, bytesRead: Int) {
        val byteBuffer = ByteBuffer.allocate(bytesRead * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until bytesRead) {
            byteBuffer.putShort(pcmBuffer[i])
        }
        randomAccessFile?.write(byteBuffer.array())
        totalAudioLen += bytesRead * 2
    }

    override fun stop() {
        totalDataLen = totalAudioLen + 36
        randomAccessFile?.seek(0)
        writeWavHeader()
        randomAccessFile?.close()
        randomAccessFile = null
    }

    private fun writeWavHeader() {
        val header = ByteArray(44)
        val channels = 1
        val byteRate = (sampleRate * channels * 16 / 8).toLong()

        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()

        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()

        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0

        header[20] = 1
        header[21] = 0

        header[22] = channels.toByte()
        header[23] = 0

        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()

        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()

        header[32] = (channels * 16 / 8).toByte()
        header[33] = 0
        header[34] = 16
        header[35] = 0

        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()

        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        randomAccessFile?.write(header)
    }
}
