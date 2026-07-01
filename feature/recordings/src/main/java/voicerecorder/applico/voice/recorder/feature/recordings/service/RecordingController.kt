package voicerecorder.applico.voice.recorder.feature.recordings.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import voicerecorder.applico.voice.recorder.core.overlay.SnackbarManager
import voicerecorder.applico.voice.recorder.core.media.recording.AudioRecordEngine
import voicerecorder.applico.voice.recorder.core.permissions.R as PermissionsR
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface RecordingController {
    val amplitudeFlow: Flow<Float>
    fun startRecording()
    fun pauseRecording()
    fun resumeRecording()
    fun resumeDraft()
    fun saveRecording()
    fun discardRecording()
    fun pinRecording(note: String? = null, timestampMs: Long? = null)
    fun pinAtAmplitudeIndex(index: Int, note: String? = null)
    suspend fun getHistoricalAmplitudes(): List<Float>
}

class RecordingControllerImpl(
    private val context: Context,
    private val snackbarManager: SnackbarManager,
    private val audioRecordEngine: AudioRecordEngine,
    private val draftManager: voicerecorder.applico.voice.recorder.data.recordings.draft.DraftManager
) : RecordingController {

    override val amplitudeFlow: Flow<Float>
        get() = audioRecordEngine.amplitudeFlow
    override fun startRecording() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            snackbarManager.showMessage(PermissionsR.string.error_permission_microphone)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                snackbarManager.showMessage(PermissionsR.string.error_permission_notification)
                return
            }
        }

        VoiceRecorderService.start(context)
    }

    override fun pauseRecording() {
        VoiceRecorderService.pause(context)
    }

    override fun resumeRecording() {
        VoiceRecorderService.resume(context)
    }

    override fun resumeDraft() {
        VoiceRecorderService.resumeDraft(context)
    }

    override fun saveRecording() {
        VoiceRecorderService.save(context)
    }
    
    override fun discardRecording() {
        VoiceRecorderService.discard(context)
    }
    
    override fun pinRecording(note: String?, timestampMs: Long?) {
        VoiceRecorderService.pin(context, note, timestampMs)
    }

    override fun pinAtAmplitudeIndex(index: Int, note: String?) {
        val sampleRate = audioRecordEngine.currentSampleRate
        val timestampMs = (index.toLong() * 2048L * 1000L) / sampleRate.toLong()
        pinRecording(note, timestampMs)
    }

    override suspend fun getHistoricalAmplitudes(): List<Float> {
        return draftManager.getHistoricalAmplitudes()
    }
}
