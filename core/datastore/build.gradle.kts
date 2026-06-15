plugins {
    id("vr.android.library")
    id("vr.android.koin")
}

android {
    namespace = "voice.recorder.recordingvoice.cct.core.datastore"
}

dependencies {
    implementation(libsCatalog.findLibrary("datastore-preferences").get())
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
