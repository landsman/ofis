package ofis.i18n

interface AppStrings {
    // ── Common ────────────────────────────────────────────────────────────────

    val appSubtitle: String
    val back: String
    val open: String
    val settings: String
    val orDragAndDrop: String
    val clearSelection: String

    // ── Settings ──────────────────────────────────────────────────────────────

    val settingsTitle: String
    val settingsLanguageLabel: String
    val settingsLanguageSystem: String
    val settingsLanguageEnglish: String
    val settingsLanguageCzech: String

    // ── Logs ──────────────────────────────────────────────────────────────────

    val logsTitle: String
    val logsClose: String
    val logsEmpty: String

    // ── PDF Compress ──────────────────────────────────────────────────────────

    val pdfCompressName: String
    val pdfCompressDescription: String
    val pdfCompressTapToSelect: String
    val pdfCompressDropHere: String
    val pdfCompressCompressionLevel: String
    val pdfCompressRecommended: String
    val pdfCompressProfileHighQuality: String
    val pdfCompressProfileHighQualityDesc: String
    val pdfCompressProfileBalanced: String
    val pdfCompressProfileBalancedDesc: String
    val pdfCompressProfileMaximum: String
    val pdfCompressProfileMaximumDesc: String
    val pdfCompressAlmostThere: String
    val pdfCompressCompressing: String
    val pdfCompressMakingSmaller: String
    val pdfCompressComplete: String
    val pdfCompressAlreadyOptimal: String
    val pdfCompressOriginal: String
    val pdfCompressCompressed: String
    val pdfCompressPercentSmaller: (percent: Int) -> String
    val pdfCompressButton: String
    val pdfCompressSave: String
    val pdfCompressCompressAnother: String
    val pdfCompressTryMaximum: String
    val pdfCompressSaveCancelled: String
    val pdfCompressFileSaved: String
    val pdfCompressNotEnoughSpace: String
    val pdfCompressPermissionDenied: String
    val pdfCompressNotEnoughSpaceDetail: (need: String, avail: String) -> String
    val pdfCompressFailedToSave: (reason: String) -> String
}
