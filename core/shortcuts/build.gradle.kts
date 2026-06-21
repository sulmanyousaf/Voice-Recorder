plugins {
    id("vr.android.library")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(project(":core:notifications"))
}
