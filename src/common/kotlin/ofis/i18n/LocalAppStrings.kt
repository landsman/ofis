package ofis.i18n

import androidx.compose.runtime.compositionLocalOf

enum class AppLanguage(val code: String) {
    SYSTEM("system"),
    ENGLISH("en"),
    CZECH("cs"),
    ;

    companion object {
        fun fromCode(code: String): AppLanguage = entries.firstOrNull { it.code == code } ?: SYSTEM
    }
}

val LocalAppStrings = compositionLocalOf<AppStrings> { EnStrings }
