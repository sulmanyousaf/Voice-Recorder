package voicerecorder.applico.voice.recorder.data.recordings.repository

import android.net.Uri
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import voicerecorder.applico.voice.recorder.core.database.dao.RecordingDao
import voicerecorder.applico.voice.recorder.core.database.entity.RecordingEntity
import voicerecorder.applico.voice.recorder.core.media.storage.MediaStoreScanner
import voicerecorder.applico.voice.recorder.core.media.storage.RecordingStorage
import java.io.File
import java.io.IOException

class RecordingRepositoryImplTest {

    private lateinit var dao: RecordingDao
    private lateinit var storage: RecordingStorage
    private lateinit var scanner: MediaStoreScanner
    private lateinit var repository: RecordingRepositoryImpl

    @Before
    fun setup() {
        dao = mockk()
        storage = mockk()
        scanner = mockk()
        repository = RecordingRepositoryImpl(dao, storage, scanner)
    }

    @Test
    fun `saveRecording success maps and inserts entity`() = runTest {
        val mockUri = mockk<Uri>()
        every { mockUri.toString() } returns "content://media/audio/123"
        
        // storage returns success
        every { storage.saveToPublicFolder(any(), any(), any()) } returns Result.success(mockUri)
        coEvery { dao.insertRecording(any()) } returns Unit

        repository.saveRecording(
            id = "test-id",
            name = "Test Recording",
            tempFilePath = "temp.wav",
            durationMs = 1000L,
            sizeBytes = 1024L,
            format = "wav"
        )

        coVerify { 
            dao.insertRecording(withArg { entity ->
                assertEquals("test-id", entity.id)
                assertEquals("Test Recording", entity.name)
                assertEquals("content://media/audio/123", entity.uriString)
                assertEquals("wav", entity.format)
            }) 
        }
    }

    @Test
    fun `saveRecording failure throws exception and does not insert`() = runTest {
        val exception = IOException("Storage full")
        every { storage.saveToPublicFolder(any(), any(), any()) } returns Result.failure(exception)

        var thrown = false
        try {
            repository.saveRecording(
                id = "test-id",
                name = "Test Recording",
                tempFilePath = "temp.wav",
                durationMs = 1000L,
                sizeBytes = 1024L,
                format = "wav"
            )
        } catch (e: Exception) {
            thrown = true
            assertEquals("Storage full", e.message)
        }
        
        assertTrue("Expected an exception to be thrown when storage fails", thrown)
        coVerify(exactly = 0) { dao.insertRecording(any()) }
    }
}
