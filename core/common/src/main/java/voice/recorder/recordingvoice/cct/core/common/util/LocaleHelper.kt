package voice.recorder.recordingvoice.cct.core.common.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import java.util.Locale

/**
 * Creates a [Context] whose resources resolve strings for the given [languageCode].
 *
 * Wrap the entire Compose tree with
 * ```
 * CompositionLocalProvider(LocalContext provides localizedContext) { ... }
 * ```
 * to update locales dynamically.
 */
fun createLocalizedContext(baseContext: Context, languageCode: String?): Context {
    if (languageCode.isNullOrEmpty()) return baseContext

    val locale = Locale.forLanguageTag(languageCode)
    Locale.setDefault(locale)

    val config = Configuration(baseContext.resources.configuration)
    config.setLocale(locale)
    config.setLocales(LocaleList(locale))

    val localeResources = baseContext.createConfigurationContext(config)

    return object : ContextWrapper(baseContext) {
        override fun getResources(): Resources = localeResources.resources
        override fun getAssets(): AssetManager = localeResources.assets
    }
}
