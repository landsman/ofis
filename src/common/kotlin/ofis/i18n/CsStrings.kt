package ofis.i18n

val CsStrings =
    AppStrings(
        common =
            CommonStrings(
                appSubtitle = "Platforma nástrojů",
                back = "← Zpět",
                open = "Otevřít",
                settings = "Nastavení",
                orDragAndDrop = "nebo přetáhněte soubor sem",
                clearSelection = "Zrušit výběr",
            ),
        logs =
            LogStrings(
                title = "Logy",
                close = "Zavřít logy",
                empty = "Zatím žádné záznamy...",
            ),
        pdfCompress =
            PdfCompressStrings(
                name = "Komprimovat PDF",
                description = "Zmenšete velikost souboru při zachování čitelnosti dokumentu.",
                tapToSelect = "Klikněte pro výběr PDF",
                dropHere = "Přetáhněte PDF sem",
                compressionLevel = "Úroveň komprese",
                recommended = "Doporučeno",
                profileHighQuality = "Vysoká kvalita",
                profileHighQualityDesc = "Bezztrátová. Zachovává všechny obrázky v plném rozlišení.",
                profileBalanced = "Vyvážená",
                profileBalancedDesc = "Doporučená. Dobrá kvalita, výrazně menší soubor.",
                profileMaximum = "Maximální komprese",
                profileMaximumDesc = "Nejmenší soubor. Obrázky budou viditelně nižší kvality.",
                almostThere = "Ještě chvíli!",
                compressing = "Komprimuji PDF...",
                makingSmaller = "Zmenšuji soubor PDF...",
                complete = "Komprese dokončena",
                alreadyOptimal = "Toto PDF je již dobře optimalizované. Komprimovaná verze není menší.",
                original = "Originál",
                compressed = "Komprimovaný",
                percentSmaller = { n -> "o $n % menší" },
                button = "Komprimovat PDF",
                save = "Uložit komprimované PDF",
                compressAnother = "Komprimovat další soubor",
                tryMaximum = "Zkusit maximální kompresi",
                saveCancelled = "Uložení zrušeno.",
                fileSaved = "Soubor byl úspěšně uložen.",
                notEnoughSpace = "Nedostatek místa na disku.",
                permissionDenied = "Přístup odepřen.",
                notEnoughSpaceDetail = { need, avail -> "Nedostatek místa. Potřeba $need, k dispozici pouze $avail." },
                failedToSave = { reason -> "Nepodařilo se uložit: $reason" },
            ),
        settings =
            SettingsStrings(
                title = "Nastavení",
                languageLabel = "Jazyk",
                languageSystem = "Systémový jazyk",
                languageEnglish = "Angličtina",
                languageCzech = "Čeština",
            ),
    )
