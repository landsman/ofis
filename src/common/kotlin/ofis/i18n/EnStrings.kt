package ofis.i18n

val EnStrings =
    AppStrings(
        common =
            CommonStrings(
                appSubtitle = "Tooling Platform",
                back = "← Back",
                open = "Open",
                settings = "Settings",
                orDragAndDrop = "or drag and drop here",
                clearSelection = "Clear selection",
            ),
        logs =
            LogStrings(
                title = "Logs",
                close = "Close logs",
                empty = "No logs yet...",
            ),
        pdfCompress =
            PdfCompressStrings(
                name = "Compress PDF",
                description = "Reduce file size while keeping your document readable.",
                tapToSelect = "Tap to select a PDF",
                dropHere = "Drop PDF here",
                compressionLevel = "Compression Level",
                recommended = "Recommended",
                profileHighQuality = "High Quality",
                profileHighQualityDesc = "Lossless. Preserves all images at full resolution.",
                profileBalanced = "Balanced",
                profileBalancedDesc = "Recommended. Good quality, noticeably smaller file.",
                profileMaximum = "Maximum Compression",
                profileMaximumDesc = "Smallest file. Images will be visibly lower quality.",
                almostThere = "Almost there!",
                compressing = "Compressing your PDF...",
                makingSmaller = "Making your PDF file smaller...",
                complete = "Compression Complete",
                alreadyOptimal = "This PDF is already well-optimized. The compressed version isn't smaller.",
                original = "Original",
                compressed = "Compressed",
                percentSmaller = { n -> "$n% smaller" },
                button = "Compress PDF",
                save = "Save compressed PDF",
                compressAnother = "Compress another file",
                tryMaximum = "Try Maximum Compression",
                saveCancelled = "Save cancelled.",
                fileSaved = "File saved successfully.",
                notEnoughSpace = "Not enough disk space.",
                permissionDenied = "Permission denied.",
                notEnoughSpaceDetail = { need, avail -> "Not enough disk space. Need $need, only $avail available." },
                failedToSave = { reason -> "Failed to save: $reason" },
            ),
        settings =
            SettingsStrings(
                title = "Settings",
                languageLabel = "Language",
                languageSystem = "System default",
                languageEnglish = "English",
                languageCzech = "Czech",
            ),
    )
