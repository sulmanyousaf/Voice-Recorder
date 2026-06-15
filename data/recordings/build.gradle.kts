plugins {
    id("vr.android.library")
    id("vr.android.koin")
}

android {
    namespace = "voice.recorder.recordingvoice.cct.data.recordings"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:media"))
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
