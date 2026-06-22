 package voicerecorder.applico.voice.recorder.feature.recordings.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import org.koin.android.ext.android.inject
import voicerecorder.applico.voice.recorder.core.datastore.AudioSettingsDataStore
import voicerecorder.applico.voice.recorder.core.media.recording.AudioFocusManager
import voicerecorder.applico.voice.recorder.core.media.recording.AudioRecordEngine
import voicerecorder.applico.voice.recorder.core.media.storage.RecordingStorage
import voicerecorder.applico.voice.recorder.core.notifications.NotificationHelper
import voicerecorder.applico.voice.recorder.data.recordings.repository.RecordingRepository
import java.io.File
import java.util.UUID

class VoiceRecorderService : Service() {

    private val notificationHelper: NotificationHelper by inject()
    private val audioSettings: AudioSettingsDataStore by inject()
    private val audioRecordEngine: AudioRecordEngine by inject()
    private val recordingStorage: RecordingStorage by inject()
    private val recordingRepository: RecordingRepository by inject()
    
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    
    private var isRecording = false
    private var isPaused = false
    private var secondsElapsed = 0L
    
    private var currentTempFile: File? = null
    private var currentRecordingId: String = ""
    private var currentFormat: String = ""

    private val audioFocusManager by lazy {
        AudioFocusManager(
            context = this,
            onPause = { pauseRecording() },
            onResume = { resumeRecording() }
        )
    }

    companion object {
        const val ACTION_START = "voicerecorder.action.START"
        const val ACTION_PAUSE = "voicerecorder.action.PAUSE"
        const val ACTION_RESUME = "voicerecorder.action.RESUME"
        const val ACTION_STOP = "voicerecorder.action.STOP"

        fun start(context: Context) {
            val intent = Intent(context, VoiceRecorderService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }

        fun pause(context: Context) {
            val intent = Intent(context, VoiceRecorderService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resume(context: Context) {
            val intent = Intent(context, VoiceRecorderService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, VoiceRecorderService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRecording()
            ACTION_PAUSE -> pauseRecording()
            ACTION_RESUME -> resumeRecording()
            ACTION_STOP -> stopRecording()
        }
        return START_STICKY
    }

    private fun startRecording() {
        if (isRecording) return
        
        serviceScope.launch {
            val settings = audioSettings.settingsFlow.first()
            val format = settings.format
            val sampleRate = settings.sampleRate
            val bitRate = settings.bitRate
            val skipSilence = settings.skipSilence
            val micBoost = settings.micBoost
            
            val tempFileResult = recordingStorage.getTempFile(format)
            if (tempFileResult.isFailure) {
                stopSelf()
                return@launch
            }
            
            if (!audioFocusManager.requestAudioFocus()) {
                stopSelf()
                return@launch
            }

            isRecording = true
            isPaused = false
            secondsElapsed = 0L
            currentFormat = format
            currentTempFile = tempFileResult.getOrThrow()
            currentRecordingId = UUID.randomUUID().toString()
            
            try {
                audioRecordEngine.skipSilence = skipSilence
                audioRecordEngine.gainFactor = if (micBoost) 2.0f else 1.0f
                audioRecordEngine.start(currentTempFile!!, format, sampleRate, bitRate)
            } catch (e: Exception) {
                // If it fails (e.g. no microphone permission), gracefully abort
                audioFocusManager.abandonAudioFocus()
                isRecording = false
                stopSelf()
                return@launch
            }
            
            updateForegroundNotification()
            startTimer()
        }
    }

    private fun pauseRecording() {
        if (!isRecording || isPaused) return
        isPaused = true
        audioRecordEngine.pause()
        updateForegroundNotification()
    }

    private fun resumeRecording() {
        if (!isRecording || !isPaused) return
        isPaused = false
        audioRecordEngine.resume()
        updateForegroundNotification()
    }

    private fun stopRecording() {
        if (!isRecording) return
        isRecording = false
        isPaused = false
        
        audioRecordEngine.stop()
        audioFocusManager.abandonAudioFocus()
        
        val tempFile = currentTempFile
        val recordingId = currentRecordingId
        val format = currentFormat
        val durationMs = secondsElapsed * 1000L
        
        if (tempFile != null && tempFile.exists()) {
            serviceScope.launch {
                try {
                    val sizeBytes = tempFile.length()
                    // Default name: "Recording yyyy-MM-dd HH-mm-ss" or similar.
                    // For now, we use a simple default name.
                    val name = "Recording_${System.currentTimeMillis()}"
                    
                    recordingRepository.saveRecording(
                        id = recordingId,
                        name = name,
                        tempFilePath = tempFile.absolutePath,
                        durationMs = durationMs,
                        sizeBytes = sizeBytes,
                        format = format
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun startTimer() {
        serviceScope.launch {
            while (isActive && isRecording) {
                if (!isPaused) {
                    secondsElapsed++
                    updateForegroundNotification()
                }
                delay(1000L)
            }
        }
    }

    private fun updateForegroundNotification() {
        val durationStr = formatDuration(secondsElapsed)
        
        val pauseResumeIntent = Intent(this, VoiceRecorderService::class.java).apply {
            action = if (isPaused) ACTION_RESUME else ACTION_PAUSE
        }
        val pauseResumePendingIntent = PendingIntent.getService(
            this, 
            1, 
            pauseResumeIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, VoiceRecorderService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 
            2, 
            stopIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = notificationHelper.buildRecordingNotification(
            isPaused = isPaused,
            durationStr = durationStr,
            pauseResumeIntent = pauseResumePendingIntent,
            stopIntent = stopPendingIntent
        )

        startForeground(NotificationHelper.NOTIFICATION_ID_RECORDING, notification)
    }

    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
