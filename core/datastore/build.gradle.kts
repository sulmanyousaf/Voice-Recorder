plugins {
    id("vr.android.library")
    id("vr.android.koin")
}

dependencies {
    implementation(libsCatalog.findLibrary("datastore-preferences").get())
    implementation(libs.androidx.appcompat)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
