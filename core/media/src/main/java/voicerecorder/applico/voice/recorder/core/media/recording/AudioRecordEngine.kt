package voicerecorder.applico.voice.recorder.core.media.recording

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import voicerecorder.applico.voice.recorder.core.media.recording.encoder.*
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import android.util.Log
import android.media.audiofx.NoiseSuppressor
import android.media.audiofx.AcousticEchoCanceler

class AudioRecordEngine(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {
    private var audioRecord: AudioRecord? = null
    private var encoder: AudioEncoder? = null
    private var recordJob: Job? = null
    private val scope = CoroutineScope(dispatcher + SupervisorJob())
    
    private var noiseSuppressor: NoiseSuppressor? = null
    private var echoCanceler: AcousticEchoCanceler? = null

    private var amplitudesOutputStream: DataOutputStream? = null

    private val _amplitudeFlow = MutableSharedFlow<Float>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val amplitudeFlow: SharedFlow<Float> = _amplitudeFlow

    private var isRecording = false
    private var isPaused = false

    var gainFactor: Float = 1.0f
    var skipSilence: Boolean = false
    var noiseReduction: Boolean = false
    private val SILENCE_THRESHOLD = 0.08f
    private var silentBufferCount = 0
    private var silentBufferLimit = 21 // Dynamically calculated later

    @SuppressLint("MissingPermission")
    fun start(outputFile: File, format: String, sampleRate: Int, bitRate: Int, append: Boolean = false, amplitudesFile: File? = null) {
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
            else -> AacEncoder(useMuxer = !append)
        }.apply {
            start(outputFile, sampleRate, bitRate, append)
        }

        if (amplitudesFile != null) {
            try {
                amplitudesOutputStream = DataOutputStream(FileOutputStream(amplitudesFile, append))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord?.release()
            audioRecord = null
            throw IllegalStateException("AudioRecord failed to initialize. Microphone permission might be missing.")
        }

        silentBufferLimit = sampleRate / 2048
        if (silentBufferLimit < 1) silentBufferLimit = 1

        isRecording = true
        isPaused = false
        silentBufferCount = 0
        
        val sessionId = audioRecord?.audioSessionId ?: 0
        if (sessionId != 0 && noiseReduction) {
            try {
                if (NoiseSuppressor.isAvailable()) {
                    noiseSuppressor = NoiseSuppressor.create(sessionId)
                    noiseSuppressor?.enabled = true
                }
                if (AcousticEchoCanceler.isAvailable()) {
                    echoCanceler = AcousticEchoCanceler.create(sessionId)
                    echoCanceler?.enabled = true
                }
            } catch (e: Exception) {
                // Ignore if device strictly restricts audiofx without specific conditions
            }
        }
        
        try {
            audioRecord?.startRecording()
        } catch (e: Exception) {
            isRecording = false
            audioRecord?.release()
            audioRecord = null
            throw e
        }

        recordJob = scope.launch {
            val buffer = ShortArray(2048)
            while (isRecording) {
                if (isPaused) {
                    delay(50)
                    continue
                }
                
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    var max = 0
                    for (i in 0 until read) {
                        var pcmVal = buffer[i].toInt()
                        if (gainFactor != 1.0f) {
                            pcmVal = (pcmVal * gainFactor).toInt()
                            if (pcmVal > 32767) pcmVal = 32767
                            else if (pcmVal < -32768) pcmVal = -32768
                            buffer[i] = pcmVal.toShort()
                        }
                        
                        val absValue = abs(pcmVal)
                        if (absValue > max) max = absValue
                    }
                    
                    val amplitude = max.toFloat() / 32767f
                    _amplitudeFlow.tryEmit(amplitude)
                    
                    try {
                        amplitudesOutputStream?.writeFloat(amplitude)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    if (skipSilence) {
                        if (amplitude < SILENCE_THRESHOLD) {
                            silentBufferCount++
                        } else {
                            silentBufferCount = 0
                        }
                        
                        if (silentBufferCount < silentBufferLimit) {
                            encoder?.encode(buffer, read)
                        }
                    } else {
                        encoder?.encode(buffer, read)
                    }
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

    suspend fun stop() {
        if (!isRecording) return
        isRecording = false
        recordJob?.join()
        recordJob = null
        
        noiseSuppressor?.release()
        noiseSuppressor = null
        echoCanceler?.release()
        echoCanceler = null

        audioRecord?.run {
            try { stop() } catch (e: Exception) { Log.e("AudioRecordEngine", "Error stopping AudioRecord", e) }
            try { release() } catch (e: Exception) { Log.e("AudioRecordEngine", "Error releasing AudioRecord", e) }
        }
        audioRecord = null

        try { encoder?.stop() } catch (e: Exception) { Log.e("AudioRecordEngine", "Error stopping encoder", e) }
        encoder = null
        
        try {
            amplitudesOutputStream?.close()
            amplitudesOutputStream = null
        } catch (e: Exception) {
            Log.e("AudioRecordEngine", "Error closing amplitudesOutputStream", e)
        }
    }
}
