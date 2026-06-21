package voicerecorder.applico.voice.recorder.feature.recordings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import voicerecorder.applico.voice.recorder.data.recordings.model.LocalRecording
import voicerecorder.applico.voice.recorder.data.recordings.repository.RecordingRepository

class RecordingsViewModel(
    private val recordingRepository: RecordingRepository
) : ViewModel() {

    val recordings: StateFlow<List<LocalRecording>> = recordingRepository
        .getSavedRecordings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
