package voicerecorder.applico.voice.recorder.core.media.recording

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import voicerecorder.applico.voice.recorder.core.media.recording.encoder.*
import java.io.File
import kotlin.math.abs

class AudioRecordEngine(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {
    private var audioRecord: AudioRecord? = null
    private var encoder: AudioEncoder? = null
    private var recordJob: Job? = null
    private val scope = CoroutineScope(dispatcher + SupervisorJob())

    private val _amplitudeFlow = MutableSharedFlow<Float>()
    val amplitudeFlow: SharedFlow<Float> = _amplitudeFlow

    private var isRecording = false
    private var isPaused = false

    @SuppressLint("MissingPermission")
    fun start(outputFile: File, format: String, sampleRate: Int, bitRate: Int) {
        if (isRecording) return

        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize * 2
        )

        encoder = when (format.uppercase()) {
            "WAV" -> WavEncoder()
            "MP3" -> Mp3Encoder()
            "AAC" -> AacEncoder(useMuxer = false)
            else -> AacEncoder(useMuxer = true)
        }.apply {
            start(outputFile, sampleRate, bitRate)
        }

        isRecording = true
        isPaused = false
        audioRecord?.startRecording()

        recordJob = scope.launch {
            val buffer = ShortArray(2048)
            while (isRecording) {
                if (isPaused) {
                    delay(50)
                    continue
                }
                
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    encoder?.encode(buffer, read)
                    
                    var max = 0
                    for (i in 0 until read) {
                        val value = abs(buffer[i].toInt())
                        if (value > max) max = value
                    }
                    _amplitudeFlow.emit(max.toFloat() / 32767f)
                }
            }
        }
    }

    fun pause() {
        isPaused = true
    }

    fun resume() {
        isPaused = false
    }

    fun stop() {
        if (!isRecording) return
        isRecording = false
        recordJob?.cancel()
        recordJob = null

        audioRecord?.run {
            stop()
            release()
        }
        audioRecord = null

        encoder?.stop()
        encoder = null
    }
}
