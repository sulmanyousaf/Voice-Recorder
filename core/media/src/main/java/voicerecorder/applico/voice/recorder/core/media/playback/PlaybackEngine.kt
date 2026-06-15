package voicerecorder.applico.voice.recorder.core.media.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlaybackEngine(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var controller: MediaController? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentMediaId = MutableStateFlow<String?>(null)
    val currentMediaId: StateFlow<String?> = _currentMediaId.asStateFlow()

    private var progressJob: Job? = null

    init {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            try {
                val mediaController = controllerFuture.get()
                controller = mediaController
                setupController(mediaController)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun setupController(controller: MediaController) {
        _isPlaying.value = controller.isPlaying
        _currentPosition.value = controller.currentPosition
        _duration.value = controller.duration
        _currentMediaId.value = controller.currentMediaItem?.mediaId
        _playbackState.value = mapPlayerState(controller.playbackState)

        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                _playbackState.value = mapPlayerState(state)
                _duration.value = controller.duration
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _currentMediaId.value = mediaItem?.mediaId
                _duration.value = controller.duration
            }
        })

        if (controller.isPlaying) {
            startProgressTracker()
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                controller?.let {
                    _currentPosition.value = it.currentPosition
                    _duration.value = it.duration
                }
                delay(200)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
        controller?.let {
            _currentPosition.value = it.currentPosition
        }
    }

    private fun mapPlayerState(state: Int): PlaybackState {
        return when (state) {
            Player.STATE_BUFFERING -> PlaybackState.BUFFERING
            Player.STATE_READY -> PlaybackState.READY
            Player.STATE_ENDED -> PlaybackState.ENDED
            else -> PlaybackState.IDLE
        }
    }

    fun play(id: String, uri: String) {
        val controller = this.controller ?: return
        val mediaItem = MediaItem.Builder()
            .setMediaId(id)
            .setUri(uri)
            .build()
        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()
    }

    fun pause() {
        controller?.pause()
    }

    fun resume() {
        controller?.play()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    fun stop() {
        controller?.stop()
    }

    fun release() {
        scope.cancel()
        controller?.release()
    }
}
