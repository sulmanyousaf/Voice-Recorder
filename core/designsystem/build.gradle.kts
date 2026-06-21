plugins {
    id("vr.android.library")
    id("vr.android.compose")
}

dependencies {
    implementation(libs.androidx.compose.material3.windowSizeClass)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
