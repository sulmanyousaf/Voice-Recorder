package voicerecorder.applico.voice.recorder.feature.recordings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import voicerecorder.applico.voice.recorder.data.recordings.model.LocalRecording
import voicerecorder.applico.voice.recorder.data.recordings.repository.RecordingRepository

import voicerecorder.applico.voice.recorder.core.database.dao.BookmarkDao
import voicerecorder.applico.voice.recorder.core.database.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

class RecordingsViewModel(
    private val recordingRepository: RecordingRepository,
    private val bookmarkDao: BookmarkDao
) : ViewModel() {

    val recordings: StateFlow<List<LocalRecording>> = recordingRepository
        .getSavedRecordings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getBookmarksFlow(recordingId: String): Flow<List<BookmarkEntity>> {
        return bookmarkDao.getBookmarksForRecordingFlow(recordingId)
    }
}
