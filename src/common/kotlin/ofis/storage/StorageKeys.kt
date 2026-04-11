package ofis.storage

/**
 * Central registry of all persistent storage keys.
 * Each entry documents what is stored and where it is used.
 */
object StorageKeys {
    /** Selected UI language (value: "en" | "cs" | "system"). Used by: LanguagePreference. */
    const val LANGUAGE = "language"
}
