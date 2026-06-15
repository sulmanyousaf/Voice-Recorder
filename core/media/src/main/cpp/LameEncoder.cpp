#include <jni.h>
#include <lame.h>
#include <android/log.h>

#define LOG_TAG "LameEncoder"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static lame_global_flags *lameFlags = NULL;

extern "C"
JNIEXPORT void JNICALL
Java_voice_recorder_recordingvoice_cct_core_media_recording_encoder_LameEncoder_init(
        JNIEnv *env, jobject thiz, jint in_sample_rate, jint out_channel,
        jint out_sample_rate, jint out_bitrate, jint quality) {
    if (lameFlags != NULL) {
        lame_close(lameFlags);
        lameFlags = NULL;
    }
    lameFlags = lame_init();
    lame_set_in_samplerate(lameFlags, in_sample_rate);
    lame_set_num_channels(lameFlags, out_channel);
    lame_set_out_samplerate(lameFlags, out_sample_rate);
    lame_set_brate(lameFlags, out_bitrate);
    lame_set_quality(lameFlags, quality);
    lame_init_params(lameFlags);
    LOGI("Lame initialized successfully");
}

extern "C"
JNIEXPORT jint JNICALL
Java_voice_recorder_recordingvoice_cct_core_media_recording_encoder_LameEncoder_encode(
        JNIEnv *env, jobject thiz, jshortArray buffer_l, jshortArray buffer_r,
        jint samples, jbyteArray mp3buf) {
    jshort *j_buffer_l = env->GetShortArrayElements(buffer_l, NULL);
    jshort *j_buffer_r = env->GetShortArrayElements(buffer_r, NULL);
    
    jsize mp3buf_size = env->GetArrayLength(mp3buf);
    jbyte *j_mp3buf = env->GetByteArrayElements(mp3buf, NULL);

    int result = lame_encode_buffer(
            lameFlags,
            j_buffer_l,
            j_buffer_r,
            samples,
            (unsigned char *)j_mp3buf,
            mp3buf_size
    );

    env->ReleaseShortArrayElements(buffer_l, j_buffer_l, 0);
    env->ReleaseShortArrayElements(buffer_r, j_buffer_r, 0);
    env->ReleaseByteArrayElements(mp3buf, j_mp3buf, 0);

    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_voice_recorder_recordingvoice_cct_core_media_recording_encoder_LameEncoder_flush(
        JNIEnv *env, jobject thiz, jbyteArray mp3buf) {
    jsize mp3buf_size = env->GetArrayLength(mp3buf);
    jbyte *j_mp3buf = env->GetByteArrayElements(mp3buf, NULL);

    int result = lame_encode_flush(lameFlags, (unsigned char *)j_mp3buf, mp3buf_size);

    env->ReleaseByteArrayElements(mp3buf, j_mp3buf, 0);
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_voice_recorder_recordingvoice_cct_core_media_recording_encoder_LameEncoder_close(
        JNIEnv *env, jobject thiz) {
    if (lameFlags != NULL) {
        lame_close(lameFlags);
        lameFlags = NULL;
        LOGI("Lame closed");
    }
}
