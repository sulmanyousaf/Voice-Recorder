package voicerecorder.applico.voice.recorder.core.media.recording.encoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File
import java.nio.ByteBuffer

class AacEncoder(private val useMuxer: Boolean = true) : AudioEncoder {
    private var mediaCodec: MediaCodec? = null
    private var mediaMuxer: MediaMuxer? = null
    private var audioTrackIndex = -1
    private val bufferInfo = MediaCodec.BufferInfo()
    private var presentationTimeUs = 0L

    override fun start(outputFile: File, sampleRate: Int, bitRate: Int) {
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
            mediaMuxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        }
    }

    override fun encode(pcmBuffer: ShortArray, bytesRead: Int) {
        val codec = mediaCodec ?: return
        val inputBufferIndex = codec.dequeueInputBuffer(10000)
        if (inputBufferIndex >= 0) {
            val inputBuffer = codec.getInputBuffer(inputBufferIndex) ?: return
            inputBuffer.clear()
            
            val byteBuffer = ByteBuffer.allocate(bytesRead * 2)
            for (i in 0 until bytesRead) {
                byteBuffer.putShort(pcmBuffer[i])
            }
            inputBuffer.put(byteBuffer.array())
            
            codec.queueInputBuffer(inputBufferIndex, 0, bytesRead * 2, presentationTimeUs, 0)
            presentationTimeUs += (bytesRead * 1000000L) / 44100
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
                    mediaMuxer?.writeSampleData(audioTrackIndex, outputBuffer, bufferInfo)
                }

                codec.releaseOutputBuffer(outputBufferIndex, false)
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    break
                }
            }
        }
    }

    override fun stop() {
        drainEncoder(true)
        mediaCodec?.run {
            stop()
            release()
        }
        mediaCodec = null
        mediaMuxer?.run {
            stop()
            release()
        }
        mediaMuxer = null
        audioTrackIndex = -1
        presentationTimeUs = 0L
    }
}
