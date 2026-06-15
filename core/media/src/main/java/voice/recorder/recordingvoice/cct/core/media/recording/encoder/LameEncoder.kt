package voice.recorder.recordingvoice.cct.core.media.recording.encoder

class LameEncoder {
    init {
        System.loadLibrary("lame-jni")
    }

    external fun init(
        inSampleRate: Int,
        outChannel: Int,
        outSampleRate: Int,
        outBitrate: Int,
        quality: Int
    )

    external fun encode(
        bufferL: ShortArray,
        bufferR: ShortArray,
        samples: Int,
        mp3buf: ByteArray
    ): Int

    external fun flush(mp3buf: ByteArray): Int

    external fun close()
}
