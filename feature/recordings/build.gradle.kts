plugins {
    id("vr.android.library")
    id("vr.android.koin")
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:datastore"))
    implementation(project(":core:notifications"))
    implementation(project(":core:media"))
    implementation(project(":core:permissions"))
    implementation(project(":core:overlay"))
    implementation(project(":core:database"))
    implementation(project(":data:recordings"))
}
