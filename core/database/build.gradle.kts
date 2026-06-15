plugins {
    id("vr.android.library")
    id("vr.android.ksp")
    id("vr.android.koin")
}

android {
    namespace = "voice.recorder.recordingvoice.cct.core.database"
}

dependencies {
    implementation(libsCatalog.findLibrary("room-runtime").get())
    implementation(libsCatalog.findLibrary("room-ktx").get())
    ksp(libsCatalog.findLibrary("room-compiler").get())
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
