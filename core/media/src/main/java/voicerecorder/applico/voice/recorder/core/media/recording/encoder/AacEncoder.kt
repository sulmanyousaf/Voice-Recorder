package voicerecorder.applico.voice.recorder.core.media.recording.encoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class AacEncoder(private val useMuxer: Boolean = true) : AudioEncoder {
    private var mediaCodec: MediaCodec? = null
    private var mediaMuxer: MediaMuxer? = null
    private var outputStream: FileOutputStream? = null
    private var audioTrackIndex = -1
    private val bufferInfo = MediaCodec.BufferInfo()
    private var presentationTimeUs = 0L
    private var sampleRate = 44100

    override fun start(outputFile: File, sampleRate: Int, bitRate: Int, append: Boolean) {
        this.sampleRate = sampleRate
        val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, 1).apply {
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 10)
        }

        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC).apply {
            configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            start()
        }

        if (useMuxer) {
            // Muxer cannot append! It will overwrite or crash. For drafts, we should use useMuxer=false
            mediaMuxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        } else {
            outputStream = FileOutputStream(outputFile, append)
        }
    }

    override fun encode(pcmBuffer: ShortArray, bytesRead: Int) {
        val codec = mediaCodec ?: return
        val inputBufferIndex = codec.dequeueInputBuffer(10000)
        if (inputBufferIndex >= 0) {
            val inputBuffer = codec.getInputBuffer(inputBufferIndex) ?: return
            inputBuffer.clear()
            
            inputBuffer.asShortBuffer().put(pcmBuffer, 0, bytesRead)
            
            codec.queueInputBuffer(inputBufferIndex, 0, bytesRead * 2, presentationTimeUs, 0)
            presentationTimeUs += (bytesRead * 1000000L) / sampleRate
        }

        drainEncoder(false)
    }

    private fun drainEncoder(endOfStream: Boolean) {
        val codec = mediaCodec ?: return
        if (endOfStream) {
            val inputBufferIndex = codec.dequeueInputBuffer(10000)
            if (inputBufferIndex >= 0) {
                codec.queueInputBuffer(inputBufferIndex, 0, 0, presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            }
        }

        while (true) {
            val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
            if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                val newFormat = codec.outputFormat
                mediaMuxer?.let {
                    audioTrackIndex = it.addTrack(newFormat)
                    it.start()
                }
            } else if (outputBufferIndex >= 0) {
                val outputBuffer = codec.getOutputBuffer(outputBufferIndex) ?: continue
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    bufferInfo.size = 0
                }
                
                if (bufferInfo.size > 0 && outputBuffer != null) {
                    outputBuffer.position(bufferInfo.offset)
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                    
                    if (useMuxer) {
                        mediaMuxer?.writeSampleData(audioTrackIndex, outputBuffer, bufferInfo)
                    } else {
                        // Write ADTS header and then the raw AAC frame
                        val outData = ByteArray(bufferInfo.size + 7)
                        addADTStoPacket(outData, outData.size)
                        outputBuffer.get(outData, 7, bufferInfo.size)
                        outputBuffer.position(bufferInfo.offset)
                        outputStream?.write(outData)
                    }
                }

                codec.releaseOutputBuffer(outputBufferIndex, false)
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    break
                }
            }
        }
    }

    override fun stop() {
        try {
            drainEncoder(true)
        } catch (e: Exception) {
            Log.e("AacEncoder", "Error in drainEncoder", e)
        }
        
        mediaCodec?.run {
            try { stop() } catch (e: Exception) { Log.e("AacEncoder", "Error stopping MediaCodec", e) }
            try { release() } catch (e: Exception) { Log.e("AacEncoder", "Error releasing MediaCodec", e) }
        }
        mediaCodec = null
        
        mediaMuxer?.run {
            try { stop() } catch (e: Exception) { Log.e("AacEncoder", "Error stopping MediaMuxer", e) }
            try { release() } catch (e: Exception) { Log.e("AacEncoder", "Error releasing MediaMuxer", e) }
        }
        mediaMuxer = null
        
        try { outputStream?.close() } catch (e: Exception) { Log.e("AacEncoder", "Error closing outputStream", e) }
        outputStream = null
        audioTrackIndex = -1
        presentationTimeUs = 0L
    }

    private fun addADTStoPacket(packet: ByteArray, packetLen: Int) {
        val profile = 2 // AAC LC
        val freqIdx = getFreqIndex(sampleRate)
        val chanCfg = 1 // Mono
        
        packet[0] = 0xFF.toByte()
        packet[1] = 0xF9.toByte()
        packet[2] = (((profile - 1) shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()
        packet[3] = (((chanCfg and 3) shl 6) + (packetLen shr 11)).toByte()
        packet[4] = ((packetLen and 0x7FF) shr 3).toByte()
        packet[5] = (((packetLen and 7) shl 5) + 0x1F).toByte()
        packet[6] = 0xFC.toByte()
    }

    private fun getFreqIndex(sampleRate: Int): Int {
        return when (sampleRate) {
            96000 -> 0
            88200 -> 1
            64000 -> 2
            48000 -> 3
            44100 -> 4
            32000 -> 5
            24000 -> 6
            22050 -> 7
            16000 -> 8
            12000 -> 9
            11025 -> 10
            8000 -> 11
            7350 -> 12
            else -> 4 // Default 44.1kHz
        }
    }
}
