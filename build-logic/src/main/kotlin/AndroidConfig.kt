import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

object AndroidConfig {
    const val COMPILE_SDK = 37
    const val TARGET_SDK = 37
    const val MIN_SDK = 26
    val JAVA_VERSION = JavaVersion.VERSION_21
}

fun Project.moduleNamespace(): String {
    val path = project.path.removePrefix(":").replace(":", ".")
    return "voicerecorder.applico.voice.recorder.$path".trimEnd('.')
}

val Project.libsCatalog: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")
