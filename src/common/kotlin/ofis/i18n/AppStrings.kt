package ofis.i18n

data class AppStrings(
    val common: CommonStrings,
    val logs: LogStrings,
    val pdfCompress: PdfCompressStrings,
    val settings: SettingsStrings,
)

data class CommonStrings(
    val appSubtitle: String,
    val back: String,
    val open: String,
    val settings: String,
    val orDragAndDrop: String,
    val clearSelection: String,
)

data class LogStrings(
    val title: String,
    val close: String,
    val empty: String,
)

data class PdfCompressStrings(
    // Tool card
    val name: String,
    val description: String,
    // File selection
    val tapToSelect: String,
    val dropHere: String,
    // Profile selector
    val compressionLevel: String,
    val recommended: String,
    val profileHighQuality: String,
    val profileHighQualityDesc: String,
    val profileBalanced: String,
    val profileBalancedDesc: String,
    val profileMaximum: String,
    val profileMaximumDesc: String,
    // Progress
    val almostThere: String,
    val compressing: String,
    val makingSmaller: String,
    // Result
    val complete: String,
    val alreadyOptimal: String,
    val original: String,
    val compressed: String,
    val percentSmaller: (percent: Int) -> String,
    // Buttons
    val button: String,
    val save: String,
    val compressAnother: String,
    val tryMaximum: String,
    // Save feedback
    val saveCancelled: String,
    val fileSaved: String,
    val notEnoughSpace: String,
    val permissionDenied: String,
    val notEnoughSpaceDetail: (need: String, avail: String) -> String,
    val failedToSave: (reason: String) -> String,
)

data class SettingsStrings(
    val title: String,
    val languageLabel: String,
    val languageSystem: String,
    val languageEnglish: String,
    val languageCzech: String,
)
