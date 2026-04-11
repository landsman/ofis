package ofis.i18n

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocaleTest {
    // Derived from AppLanguage — adding a new language automatically includes it here.
    // SYSTEM is excluded because it aliases EN and would produce a false "untranslated" failure.
    private val allLocales: List<Pair<String, AppStrings>> =
        AppLanguage.entries
            .filter { it != AppLanguage.SYSTEM }
            .map { it.code.uppercase() to it.toAppStrings() }

    private val enStrings: AppStrings = AppLanguage.ENGLISH.toAppStrings()

    private val stringProps: List<KProperty1<AppStrings, *>> =
        AppStrings::class.memberProperties.filter { it.returnType.classifier == String::class }

    @Test
    fun noEmptyStrings() {
        for ((localeName, locale) in allLocales) {
            for (prop in stringProps) {
                val value = prop.get(locale) as String
                assertTrue(value.isNotBlank(), "$localeName.${prop.name} is blank")
            }
        }
    }

    @Test
    fun lambdasReturnNonEmptyStrings() {
        for ((localeName, locale) in allLocales) {
            assertTrue(
                locale.pdfCompressPercentSmaller(42).isNotBlank(),
                "$localeName.pdfCompressPercentSmaller returned blank",
            )
            assertTrue(
                locale.pdfCompressNotEnoughSpaceDetail("1 MB", "500 KB").isNotBlank(),
                "$localeName.pdfCompressNotEnoughSpaceDetail returned blank",
            )
            assertTrue(
                locale.pdfCompressFailedToSave("disk full").isNotBlank(),
                "$localeName.pdfCompressFailedToSave returned blank",
            )
        }
    }

    @Test
    fun nonEnglishLocalesHaveNoUntranslatedStrings() {
        val nonEnglish = allLocales.filter { (_, locale) -> locale !== enStrings }
        for ((localeName, locale) in nonEnglish) {
            val untranslated =
                stringProps
                    .filter { prop -> prop.get(enStrings) == prop.get(locale) }
                    .map { it.name }

            assertEquals(
                emptyList(),
                untranslated,
                "$localeName strings identical to EN (likely untranslated): $untranslated",
            )
        }
    }
}
