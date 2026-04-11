package ofis.i18n

object CsStrings : AppStrings {
    // ── Common ────────────────────────────────────────────────────────────────
    override val appSubtitle = "Platforma nástrojů"
    override val back = "← Zpět"
    override val open = "Otevřít"
    override val settings = "Nastavení"
    override val orDragAndDrop = "nebo přetáhněte soubor sem"
    override val clearSelection = "Zrušit výběr"

    // ── Logs ──────────────────────────────────────────────────────────────────
    override val logsTitle = "Logy"
    override val logsClose = "Zavřít logy"
    override val logsEmpty = "Zatím žádné záznamy..."

    // ── PDF Compress ──────────────────────────────────────────────────────────
    override val pdfCompressName = "Komprimovat PDF"
    override val pdfCompressDescription = "Zmenšete velikost souboru při zachování čitelnosti dokumentu."
    override val pdfCompressTapToSelect = "Klikněte pro výběr PDF"
    override val pdfCompressDropHere = "Přetáhněte PDF sem"
    override val pdfCompressCompressionLevel = "Úroveň komprese"
    override val pdfCompressRecommended = "Doporučeno"
    override val pdfCompressProfileHighQuality = "Vysoká kvalita"
    override val pdfCompressProfileHighQualityDesc = "Bezztrátová. Zachovává všechny obrázky v plném rozlišení."
    override val pdfCompressProfileBalanced = "Vyvážená"
    override val pdfCompressProfileBalancedDesc = "Doporučená. Dobrá kvalita, výrazně menší soubor."
    override val pdfCompressProfileMaximum = "Maximální komprese"
    override val pdfCompressProfileMaximumDesc = "Nejmenší soubor. Obrázky budou viditelně nižší kvality."
    override val pdfCompressAlmostThere = "Ještě chvíli!"
    override val pdfCompressCompressing = "Komprimuji PDF..."
    override val pdfCompressMakingSmaller = "Zmenšuji soubor PDF..."
    override val pdfCompressComplete = "Komprese dokončena"
    override val pdfCompressAlreadyOptimal = "Toto PDF je již dobře optimalizované. Komprimovaná verze není menší."
    override val pdfCompressOriginal = "Originál"
    override val pdfCompressCompressed = "Komprimovaný"
    override val pdfCompressPercentSmaller = { n: Int -> "o $n % menší" }
    override val pdfCompressButton = "Komprimovat PDF"
    override val pdfCompressSave = "Uložit komprimované PDF"
    override val pdfCompressCompressAnother = "Komprimovat další soubor"
    override val pdfCompressTryMaximum = "Zkusit maximální kompresi"
    override val pdfCompressSaveCancelled = "Uložení zrušeno."
    override val pdfCompressFileSaved = "Soubor byl úspěšně uložen."
    override val pdfCompressNotEnoughSpace = "Nedostatek místa na disku."
    override val pdfCompressPermissionDenied = "Přístup odepřen."
    override val pdfCompressNotEnoughSpaceDetail = {
        need: String,
        avail: String,
        ->
        "Nedostatek místa. Potřeba $need, k dispozici pouze $avail."
    }
    override val pdfCompressFailedToSave = { reason: String -> "Nepodařilo se uložit: $reason" }

    // ── Settings ──────────────────────────────────────────────────────────────
    override val settingsTitle = "Nastavení"
    override val settingsLanguageLabel = "Jazyk"
    override val settingsLanguageSystem = "Systémový jazyk"
    override val settingsLanguageEnglish = "Angličtina"
    override val settingsLanguageCzech = "Čeština"
}
