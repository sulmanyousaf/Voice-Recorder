plugins {
    id("vr.android.application")
    id("vr.android.compose")
    id("vr.android.koin")
}

dependencies {
    // Core Modules
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:media"))
    implementation(project(":data:recordings"))
    implementation(project(":core:navigation"))
    implementation(project(":core:notifications"))
    implementation(project(":core:overlay"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}