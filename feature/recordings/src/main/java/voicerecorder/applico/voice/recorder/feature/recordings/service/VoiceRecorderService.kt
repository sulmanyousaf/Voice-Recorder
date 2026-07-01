package voicerecorder.applico.voice.recorder.feature.recordings.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
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
import voicerecorder.applico.voice.recorder.core.database.dao.BookmarkDao
import voicerecorder.applico.voice.recorder.core.database.entity.BookmarkEntity
import voicerecorder.applico.voice.recorder.data.recordings.repository.RecordingRepository
import voicerecorder.applico.voice.recorder.data.recordings.draft.DraftManager
import voicerecorder.applico.voice.recorder.data.recordings.draft.DraftMetadata
import voicerecorder.applico.voice.recorder.core.media.storage.AudioTagger
import voicerecorder.applico.voice.recorder.core.media.storage.AudioPin
import java.io.File
import java.util.UUID

class VoiceRecorderService : Service() {

    private val notificationHelper: NotificationHelper by inject()
    private val audioSettings: AudioSettingsDataStore by inject()
    private val audioRecordEngine: AudioRecordEngine by inject()
    private val recordingStorage: RecordingStorage by inject()
    private val recordingRepository: RecordingRepository by inject()
    private val bookmarkDao: BookmarkDao by inject()
    private val draftManager: DraftManager by inject()
    private val audioTagger: AudioTagger by inject()
    
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
        const val ACTION_SAVE = "voicerecorder.action.SAVE"
        const val ACTION_DISCARD = "voicerecorder.action.DISCARD"
        const val ACTION_PIN = "voicerecorder.action.PIN"
        const val ACTION_RESUME_DRAFT = "voicerecorder.action.RESUME_DRAFT"

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

        fun resumeDraft(context: Context) {
            val intent = Intent(context, VoiceRecorderService::class.java).apply {
                action = ACTION_RESUME_DRAFT
            }
            context.startForegroundService(intent)
        }

        fun save(context: Context) {
            val intent = Intent(context, VoiceRecorderService::class.java).apply {
                action = ACTION_SAVE
            }
            context.startService(intent)
        }

        fun discard(context: Context) {
            val intent = Intent(context, VoiceRecorderService::class.java).apply {
                action = ACTION_DISCARD
            }
            context.startService(intent)
        }
        
        fun pin(context: Context) {
            val intent = Intent(context, VoiceRecorderService::class.java).apply {
                action = ACTION_PIN
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
            ACTION_SAVE -> saveRecording()
            ACTION_DISCARD -> discardRecording()
            ACTION_PIN -> pinRecording()
            ACTION_RESUME_DRAFT -> resumeDraft()
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
            val noiseReduction = settings.noiseReduction
            
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
                draftManager.saveMetadata(
                    DraftMetadata(
                        recordingId = currentRecordingId,
                        format = format,
                        durationMs = 0L,
                        sampleRate = sampleRate,
                        bitRate = bitRate
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            try {
                audioRecordEngine.skipSilence = skipSilence
                audioRecordEngine.gainFactor = if (micBoost) 2.0f else 1.0f
                audioRecordEngine.noiseReduction = noiseReduction
                audioRecordEngine.start(currentTempFile!!, format, sampleRate, bitRate)
            } catch (e: Exception) {
                e.printStackTrace()
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

    private fun resumeDraft() {
        if (isRecording) return
        
        serviceScope.launch {
            val metadata = draftManager.getMetadata()
            val tempFileResult = draftManager.getDraftFile()
            val amplitudesFile = draftManager.getAmplitudesFile()
            
            if (metadata == null || tempFileResult == null) {
                stopSelf()
                return@launch
            }
            
            if (!audioFocusManager.requestAudioFocus()) {
                stopSelf()
                return@launch
            }
            
            val settings = audioSettings.settingsFlow.first()
            val skipSilence = settings.skipSilence
            val micBoost = settings.micBoost
            val noiseReduction = settings.noiseReduction

            isRecording = true
            isPaused = false
            secondsElapsed = metadata.durationMs / 1000L
            currentFormat = metadata.format
            currentTempFile = tempFileResult
            currentRecordingId = metadata.recordingId
            
            try {
                audioRecordEngine.skipSilence = skipSilence
                audioRecordEngine.gainFactor = if (micBoost) 2.0f else 1.0f
                audioRecordEngine.noiseReduction = noiseReduction
                audioRecordEngine.start(
                    outputFile = currentTempFile!!,
                    format = metadata.format,
                    sampleRate = metadata.sampleRate,
                    bitRate = metadata.bitRate,
                    append = true,
                    amplitudesFile = amplitudesFile
                )
            } catch (e: Exception) {
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

    private fun pinRecording() {
        if (!isRecording) return
        val recordingId = currentRecordingId
        val timestampMs = secondsElapsed * 1000L
        serviceScope.launch {
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    recordingId = recordingId,
                    timestampMs = timestampMs,
                    noteText = "Pinned at ${formatDuration(secondsElapsed)}"
                )
            )
        }
    }

    private fun saveRecording() {
        if (!isRecording) return
        isRecording = false
        isPaused = false
        
        audioFocusManager.abandonAudioFocus()
        
        val tempFile = currentTempFile
        val recordingId = currentRecordingId
        val format = currentFormat
        val durationMs = secondsElapsed * 1000L
        
        if (tempFile != null && tempFile.exists()) {
            serviceScope.launch {
                try {
                    audioRecordEngine.stop()
                    var finalTempPath = tempFile.absolutePath
                    
                    if (format.equals("M4A", ignoreCase = true) || format.equals("AAC", ignoreCase = true)) {
                        val muxedFile = File(tempFile.parent, "muxed_temp.m4a")
                        if (muxAacToM4a(tempFile, muxedFile)) {
                            finalTempPath = muxedFile.absolutePath
                        }
                    }
                    
                    val sizeBytes = File(finalTempPath).length()
                    val name = "Recording_${System.currentTimeMillis()}"
                    
                    val pins = bookmarkDao.getBookmarksForRecording(recordingId)
                    val audioPins = pins.map { AudioPin(it.timestampMs, it.noteText) }
                    audioTagger.injectPins(File(finalTempPath), audioPins)
                    
                    recordingRepository.saveRecording(
                        id = recordingId,
                        name = name,
                        tempFilePath = finalTempPath,
                        durationMs = durationMs,
                        sizeBytes = sizeBytes,
                        format = format
                    )
                } catch (e: Exception) {
                    Log.e("VoiceRecorderService", "Error saving recording", e)
                } finally {
                    draftManager.discardDraft()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        } else {
            serviceScope.launch {
                audioRecordEngine.stop()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun muxAacToM4a(inputFile: File, outputFile: File): Boolean {
        try {
            val extractor = android.media.MediaExtractor()
            extractor.setDataSource(inputFile.absolutePath)
            
            var audioTrackIndex = -1
            for (i in 0 until extractor.trackCount) {
                val fmt = extractor.getTrackFormat(i)
                val mime = fmt.getString(android.media.MediaFormat.KEY_MIME)
                if (mime?.startsWith("audio/") == true) {
                    audioTrackIndex = i
                    break
                }
            }
            if (audioTrackIndex < 0) return false
            
            extractor.selectTrack(audioTrackIndex)
            val format = extractor.getTrackFormat(audioTrackIndex)
            
            val muxer = android.media.MediaMuxer(outputFile.absolutePath, android.media.MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val muxerTrackIndex = muxer.addTrack(format)
            muxer.start()
            
            val bufferSize = format.getInteger(android.media.MediaFormat.KEY_MAX_INPUT_SIZE, 1048576)
            val buffer = java.nio.ByteBuffer.allocate(bufferSize)
            val bufferInfo = android.media.MediaCodec.BufferInfo()
            
            while (true) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break
                
                bufferInfo.offset = 0
                bufferInfo.size = sampleSize
                bufferInfo.presentationTimeUs = extractor.sampleTime
                bufferInfo.flags = extractor.sampleFlags
                
                muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                extractor.advance()
            }
            
            muxer.stop()
            muxer.release()
            extractor.release()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun discardRecording() {
        if (!isRecording) return
        isRecording = false
        isPaused = false
        
        audioFocusManager.abandonAudioFocus()
        
        serviceScope.launch {
            audioRecordEngine.stop()
            draftManager.discardDraft()
            bookmarkDao.deleteBookmarksForRecording(currentRecordingId)
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
                    
                    try {
                        val md = draftManager.getMetadata()
                        if (md != null) {
                            draftManager.saveMetadata(md.copy(durationMs = secondsElapsed * 1000L))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                delay(1000L)
            }
        }
    }

    private fun updateForegroundNotification() {
        val durationStr = formatDuration(secondsElapsed)
        
        val pinIntent = Intent(this, VoiceRecorderService::class.java).apply {
            action = ACTION_PIN
        }
        val pinPendingIntent = PendingIntent.getService(
            this,
            1,
            pinIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeIntent = Intent(this, VoiceRecorderService::class.java).apply {
            action = if (isPaused) ACTION_RESUME else ACTION_PAUSE
        }
        val pauseResumePendingIntent = PendingIntent.getService(
            this, 
            2, 
            pauseResumeIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val saveIntent = Intent(this, VoiceRecorderService::class.java).apply {
            action = ACTION_SAVE
        }
        val savePendingIntent = PendingIntent.getService(
            this, 
            3, 
            saveIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val discardIntent = Intent(this, VoiceRecorderService::class.java).apply {
            action = ACTION_DISCARD
        }
        val discardPendingIntent = PendingIntent.getService(
            this, 
            4, 
            discardIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = notificationHelper.buildRecordingNotification(
            isPaused = isPaused,
            durationStr = durationStr,
            pinIntent = pinPendingIntent,
            pauseResumeIntent = pauseResumePendingIntent,
            saveIntent = savePendingIntent,
            discardIntent = discardPendingIntent
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NotificationHelper.NOTIFICATION_ID_RECORDING, 
                notification, 
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NotificationHelper.NOTIFICATION_ID_RECORDING, notification)
        }
    }

    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format(java.util.Locale.US, "%02d:%02d", minutes, secs)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
