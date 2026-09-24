package ofis.i18n

import ofis.storage.AppStorage
import ofis.storage.StorageKeys

fun loadLanguagePreference(): AppLanguage =
    AppStorage.read(StorageKeys.LANGUAGE)?.let { AppLanguage.fromCode(it) } ?: AppLanguage.SYSTEM

fun saveLanguagePreference(language: AppLanguage) = AppStorage.write(StorageKeys.LANGUAGE, language.code)

fun AppLanguage.toAppStrings(): AppStrings =
    when (this) {
        AppLanguage.CZECH -> CsStrings
        AppLanguage.ENGLISH, AppLanguage.SYSTEM -> EnStrings
    }
