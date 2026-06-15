plugins {
    id("vr.android.library")
    id("vr.android.koin")
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}