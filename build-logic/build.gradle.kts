plugins {
    `kotlin-dsl`
}

val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    compileOnly(versionCatalog.findLibrary("android-gradle-plugin").get())
    compileOnly(versionCatalog.findLibrary("kotlin-gradle-plugin").get())
    compileOnly(versionCatalog.findLibrary("ksp-gradle-plugin").get())
    compileOnly(versionCatalog.findLibrary("compose-compiler-gradle-plugin").get())
    compileOnly(versionCatalog.findLibrary("kotlin-serialization-gradle-plugin").get())
}
