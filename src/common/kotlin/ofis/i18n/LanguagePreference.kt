package ofis.i18n

import ofis.platform.fileSystem
import okio.Path.Companion.toPath

private const val PREF_FILE = "ofis_language.txt"

fun loadLanguagePreference(): AppLanguage {
    return try {
        val path = PREF_FILE.toPath()
        val code = fileSystem.read(path) { readUtf8() }.trim()
        AppLanguage.fromCode(code)
    } catch (_: Exception) {
        AppLanguage.SYSTEM
    }
}

fun saveLanguagePreference(language: AppLanguage) {
    try {
        val path = PREF_FILE.toPath()
        fileSystem.write(path) { writeUtf8(language.code) }
    } catch (_: Exception) {
        // non-critical — preference loss is acceptable
    }
}

fun AppLanguage.toAppStrings(): AppStrings =
    when (this) {
        AppLanguage.CZECH -> CsStrings
        AppLanguage.ENGLISH, AppLanguage.SYSTEM -> EnStrings
    }
