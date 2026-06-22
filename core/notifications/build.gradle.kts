plugins {
    id("vr.android.library")
    id("vr.android.koin")
}

dependencies {
    implementation(project(":core:common"))
    implementation(libsCatalog.findLibrary("androidx-core-ktx").get())
    implementation(libsCatalog.findLibrary("androidx-media").get())
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
