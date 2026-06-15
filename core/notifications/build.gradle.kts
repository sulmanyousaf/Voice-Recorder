plugins {
    id("vr.android.library")
    id("vr.android.koin")
}

android {
    namespace = "voice.recorder.recordingvoice.cct.core.notifications"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libsCatalog.findLibrary("androidx-core-ktx").get())
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
