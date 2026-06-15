plugins {
    id("vr.android.library")
    id("vr.android.koin")
}

android {
    namespace = "voice.recorder.recordingvoice.cct.core.media"
    
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:datastore"))
    implementation(project(":core:notifications"))
    implementation(libsCatalog.findLibrary("androidx-media3-exoplayer").get())
    implementation(libsCatalog.findLibrary("androidx-media3-common").get())
    implementation(libsCatalog.findLibrary("androidx-media3-session").get())
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
