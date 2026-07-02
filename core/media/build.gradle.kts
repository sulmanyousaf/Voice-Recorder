plugins {
    id("vr.android.library")
    id("vr.android.koin")
}

android {
    ndkVersion = "30.0.14904198"
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "4.1.2"
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
    implementation(libsCatalog.findLibrary("jaudiotagger").get())
    implementation(libsCatalog.findLibrary("mp4parser").get())
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
