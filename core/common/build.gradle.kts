plugins {
    id("vr.android.library")
    id("vr.android.koin")
}

android {
    namespace = "voice.recorder.recordingvoice.cct.core.common"
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}