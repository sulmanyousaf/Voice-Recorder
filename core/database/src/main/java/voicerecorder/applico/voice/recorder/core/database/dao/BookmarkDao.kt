package voicerecorder.applico.voice.recorder.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import voicerecorder.applico.voice.recorder.core.database.entity.BookmarkEntity

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("SELECT * FROM bookmarks WHERE recordingId = :recordingId ORDER BY timestampMs ASC")
    fun getBookmarksForRecordingFlow(recordingId: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE recordingId = :recordingId ORDER BY timestampMs ASC")
    suspend fun getBookmarksForRecording(recordingId: String): List<BookmarkEntity>

    @Query("DELETE FROM bookmarks WHERE recordingId = :recordingId")
    suspend fun deleteBookmarksForRecording(recordingId: String)
}
