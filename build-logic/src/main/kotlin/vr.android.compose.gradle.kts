import com.android.build.api.dsl.CommonExtension

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

extensions.configure<CommonExtension> {
    buildFeatures.compose = true
}

dependencies {
    val bom = platform(libsCatalog.findLibrary("androidx-compose-bom").get())
    "implementation"(bom)
    "implementation"(libsCatalog.findLibrary("androidx-compose-ui").get())
    "implementation"(libsCatalog.findLibrary("androidx-compose-ui-graphics").get())
    "implementation"(libsCatalog.findLibrary("androidx-compose-ui-tooling-preview").get())
    "implementation"(libsCatalog.findLibrary("androidx-compose-material3").get())
    "debugImplementation"(libsCatalog.findLibrary("androidx-compose-ui-tooling").get())
    "debugImplementation"(libsCatalog.findLibrary("androidx-compose-ui-test-manifest").get())
    "androidTestImplementation"(bom)
    "androidTestImplementation"(libsCatalog.findLibrary("androidx-compose-ui-test-junit4").get())
}
