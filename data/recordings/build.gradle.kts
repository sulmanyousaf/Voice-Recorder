plugins {
    id("vr.android.library")
    id("vr.android.koin")
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:media"))
    
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
