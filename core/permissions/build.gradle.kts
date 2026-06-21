plugins {
    id("vr.android.library")
    id("vr.android.compose")
}

dependencies {
    implementation(project(":core:overlay"))
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.activity.compose)
}
