plugins {
    id("vr.android.library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    implementation(libsCatalog.findLibrary("kotlinx-serialization-json").get())
    implementation(libsCatalog.findLibrary("androidx-navigation3-runtime").get())
}
