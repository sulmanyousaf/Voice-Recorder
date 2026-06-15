plugins {
    id("vr.android.library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "voice.recorder.recordingvoice.cct.core.navigation"
}

dependencies {
    implementation(libsCatalog.findLibrary("kotlinx-serialization-json").get())
    implementation(libsCatalog.findLibrary("androidx-navigation3-runtime").get())
}
