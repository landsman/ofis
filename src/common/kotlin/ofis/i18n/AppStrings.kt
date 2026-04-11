package ofis.i18n

data class AppStrings(
    // App chrome
    val appSubtitle: String,
    val back: String,
    val open: String,
    val settings: String,
    // Logs
    val logsTitle: String,
    val closeLogs: String,
    val noLogsYet: String,
    // File drop zone
    val orDragAndDrop: String,
    val clearSelection: String,
    // PDF compress — general
    val compressPdf: String,
    val compressPdfDescription: String,
    val tapToSelectPdf: String,
    val dropPdfHere: String,
    // PDF compress — profile selector
    val compressionLevel: String,
    val recommended: String,
    val profileHighQuality: String,
    val profileHighQualityDesc: String,
    val profileBalanced: String,
    val profileBalancedDesc: String,
    val profileMaximum: String,
    val profileMaximumDesc: String,
    // PDF compress — progress
    val almostThere: String,
    val compressingPdf: String,
    val makingPdfSmaller: String,
    // PDF compress — result
    val compressionComplete: String,
    val alreadyOptimal: String,
    val original: String,
    val compressedLabel: String,
    val percentSmaller: (percent: Int) -> String,
    // PDF compress — buttons
    val compressPdfButton: String,
    val saveCompressedPdf: String,
    val compressAnotherFile: String,
    val tryMaximumCompression: String,
    // Save messages
    val saveCancelled: String,
    val fileSaved: String,
    val notEnoughSpace: String,
    val permissionDenied: String,
    val notEnoughSpaceDetail: (need: String, avail: String) -> String,
    val failedToSave: (reason: String) -> String,
    // Settings
    val settingsTitle: String,
    val languageLabel: String,
    val languageSystem: String,
    val languageEnglish: String,
    val languageCzech: String,
)
