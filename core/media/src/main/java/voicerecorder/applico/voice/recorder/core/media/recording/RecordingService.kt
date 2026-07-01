package voicerecorder.applico.voice.recorder.core.media.recording

import android.Manifest
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.seconds
import org.koin.android.ext.android.inject
import voicerecorder.applico.voice.recorder.core.media.storage.RecordingStorage
import voicerecorder.applico.voice.recorder.core.notifications.RecordingNotificationManager
import java.io.File

class RecordingService : Service() {
    private val recordingEngine: AudioRecordEngine by inject()
    private val notificationManager: RecordingNotificationManager by inject()
    private val storage: RecordingStorage by inject()
    private val serviceManager: RecordingServiceManager by inject()

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var durationSeconds = 0
    private var timerJob: Job? = null
    private var audioFocusManager: AudioFocusManager? = null
    private var isBluetoothScoStarted = false

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            enableBluetoothAudio(audioManager)
        }
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_FORMAT = "EXTRA_FORMAT"
    }

    override fun onCreate() {
        super.onCreate()
        audioFocusManager = AudioFocusManager(
            context = this,
            onPause = { recordingEngine.pause() },
            onResume = { recordingEngine.resume() }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val format = intent.getStringExtra(EXTRA_FORMAT) ?: "M4A"
                startRecording(format)
            }
            ACTION_STOP -> {
                stopRecording()
            }
        }
        return START_NOT_STICKY
    }

    private fun startRecording(format: String) {
        val tempFileResult = storage.getTempFile(format)
        val tempFile = tempFileResult.getOrElse {
            // Storage full or permission denied
            stopSelf()
            return
        }
        
        audioFocusManager?.requestAudioFocus()
        setupBluetoothSco()
        
        val notification = notificationManager.buildNotification("00:00")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                RecordingNotificationManager.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(RecordingNotificationManager.NOTIFICATION_ID, notification)
        }

        recordingEngine.start(tempFile, format, 44100, 128000)
        serviceManager.updateState(RecordingState.Recording(0, FloatArray(0)))
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        durationSeconds = 0
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1.seconds)
                durationSeconds++
                val minutes = durationSeconds / 60
                val seconds = durationSeconds % 60
                val durationText = String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds)
                val amplitudes = FloatArray(0)
                notificationManager.updateNotification(durationText)
                serviceManager.updateState(RecordingState.Recording(durationSeconds, amplitudes))
            }
        }
    }

    private fun enableBluetoothAudio(audioManager: AudioManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val devices = audioManager.availableCommunicationDevices
            val bluetoothDevice = devices.firstOrNull {
                it.type == android.media.AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                it.type == android.media.AudioDeviceInfo.TYPE_BLE_HEADSET
            }
            if (bluetoothDevice != null) {
                try {
                    audioManager.setCommunicationDevice(bluetoothDevice)
                    isBluetoothScoStarted = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            @Suppress("DEPRECATION")
            if (audioManager.isBluetoothScoAvailableOffCall) {
                try {
                    @Suppress("DEPRECATION")
                    audioManager.startBluetoothSco()
                    @Suppress("DEPRECATION")
                    audioManager.isBluetoothScoOn = true
                    isBluetoothScoStarted = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun disableBluetoothAudio(audioManager: AudioManager) {
        if (isBluetoothScoStarted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    audioManager.clearCommunicationDevice()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                try {
                    @Suppress("DEPRECATION")
                    audioManager.stopBluetoothSco()
                    @Suppress("DEPRECATION")
                    audioManager.isBluetoothScoOn = false
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            isBluetoothScoStarted = false
        }
    }

    @Suppress("DEPRECATION")
    private fun setupBluetoothSco() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        // Register receiver for bluetooth headset connection changes and SCO audio updates
        val filter = IntentFilter().apply {
            addAction(android.bluetooth.BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
            addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
        }
        registerReceiver(bluetoothReceiver, filter)

        // Try to enable SCO immediately if a Bluetooth headset is already connected
        if (hasBluetoothPermission()) {
            enableBluetoothAudio(audioManager)
        }
    }

    private fun teardownBluetoothSco() {
        try {
            unregisterReceiver(bluetoothReceiver)
        } catch (e: Exception) {
            // Ignored if not registered
        }

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        disableBluetoothAudio(audioManager)
    }

    private fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun stopRecording() {
        timerJob?.cancel()
        audioFocusManager?.abandonAudioFocus()
        teardownBluetoothSco()
        serviceScope.launch {
            recordingEngine.stop()
            stopForeground(STOP_FOREGROUND_REMOVE)
            serviceManager.updateState(RecordingState.Idle)
            stopSelf()
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
