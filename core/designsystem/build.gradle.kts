plugins {
    id("vr.android.library")
    id("vr.android.compose")
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
