package ofis.i18n

enum class AppLanguage(
    val code: String,
) {
    SYSTEM("system"),
    ENGLISH("en"),
    CZECH("cs"),
    ;

    companion object {
        fun fromCode(code: String): AppLanguage = entries.firstOrNull { it.code == code } ?: SYSTEM
    }
}
