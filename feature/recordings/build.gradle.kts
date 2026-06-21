plugins {
    id("vr.android.library")
    id("vr.android.koin")
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":data:recordings"))
}
