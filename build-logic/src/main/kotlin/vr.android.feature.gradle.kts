plugins {
    id("vr.android.library")
    id("vr.android.compose")
    id("vr.android.koin")
    id("vr.android.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    "implementation"(libsCatalog.findLibrary("androidx-navigation3-runtime").get())
    "implementation"(libsCatalog.findLibrary("androidx-navigation3-ui").get())
    "implementation"(libsCatalog.findLibrary("androidx-lifecycle-viewmodel-navigation3").get())
    "implementation"(libsCatalog.findLibrary("kotlinx-serialization-json").get())
}
