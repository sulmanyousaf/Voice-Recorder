plugins {
    id("vr.android.library")
    id("vr.android.compose")
}

android {
    namespace = "voice.recorder.recordingvoice.cct.core.designsystem"
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
