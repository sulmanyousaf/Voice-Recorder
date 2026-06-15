package voice.recorder.recordingvoice.cct.core.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import voice.recorder.recordingvoice.cct.core.database.entity.RecordingEntity

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recordings ORDER BY createdAtMs DESC")
    fun getAllRecordings(): Flow<List<RecordingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: RecordingEntity)

    @Delete
    suspend fun deleteRecording(recording: RecordingEntity)
}
