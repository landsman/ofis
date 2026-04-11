package ofis.i18n

interface AppStrings {
    // ── Common ────────────────────────────────────────────────────────────────

    /** "Tooling Platform" */
    val appSubtitle: String

    /** "← Back" */
    val back: String

    /** "Open" */
    val open: String

    /** "Settings" */
    val settings: String

    /** "or drag and drop here" */
    val orDragAndDrop: String

    /** "Clear selection" */
    val clearSelection: String

    // ── Settings ──────────────────────────────────────────────────────────────

    /** "Settings" */
    val settingsTitle: String

    /** "Language" */
    val settingsLanguageLabel: String

    /** "System default" */
    val settingsLanguageSystem: String

    /** "English" */
    val settingsLanguageEnglish: String

    /** "Czech" */
    val settingsLanguageCzech: String

    // ── Logs ──────────────────────────────────────────────────────────────────

    /** "Logs" */
    val logsTitle: String

    /** "Close logs" */
    val logsClose: String

    /** "No logs yet..." */
    val logsEmpty: String

    // ── PDF Compress ──────────────────────────────────────────────────────────

    /** "Compress PDF" */
    val pdfCompressName: String

    /** "Reduce file size while keeping your document readable." */
    val pdfCompressDescription: String

    /** "Tap to select a PDF" */
    val pdfCompressTapToSelect: String

    /** "Drop PDF here" */
    val pdfCompressDropHere: String

    /** "Compression Level" */
    val pdfCompressCompressionLevel: String

    /** "Recommended" */
    val pdfCompressRecommended: String

    /** "High Quality" */
    val pdfCompressProfileHighQuality: String

    /** "Lossless. Preserves all images at full resolution." */
    val pdfCompressProfileHighQualityDesc: String

    /** "Balanced" */
    val pdfCompressProfileBalanced: String

    /** "Recommended. Good quality, noticeably smaller file." */
    val pdfCompressProfileBalancedDesc: String

    /** "Maximum Compression" */
    val pdfCompressProfileMaximum: String

    /** "Smallest file. Images will be visibly lower quality." */
    val pdfCompressProfileMaximumDesc: String

    /** "Almost there!" */
    val pdfCompressAlmostThere: String

    /** "Compressing your PDF..." */
    val pdfCompressCompressing: String

    /** "Making your PDF file smaller..." */
    val pdfCompressMakingSmaller: String

    /** "Compression Complete" */
    val pdfCompressComplete: String

    /** "This PDF is already well-optimized. The compressed version isn't smaller." */
    val pdfCompressAlreadyOptimal: String

    /** "Original" */
    val pdfCompressOriginal: String

    /** "Compressed" */
    val pdfCompressCompressed: String

    /** "$n% smaller" */
    val pdfCompressPercentSmaller: (percent: Int) -> String

    /** "Compress PDF" */
    val pdfCompressButton: String

    /** "Save compressed PDF" */
    val pdfCompressSave: String

    /** "Compress another file" */
    val pdfCompressCompressAnother: String

    /** "Try Maximum Compression" */
    val pdfCompressTryMaximum: String

    /** "Save cancelled." */
    val pdfCompressSaveCancelled: String

    /** "File saved successfully." */
    val pdfCompressFileSaved: String

    /** "Not enough disk space." */
    val pdfCompressNotEnoughSpace: String

    /** "Permission denied." */
    val pdfCompressPermissionDenied: String

    /** "Not enough disk space. Need {need}, only {avail} available." */
    val pdfCompressNotEnoughSpaceDetail: (need: String, avail: String) -> String

    /** "Failed to save: {reason}" */
    val pdfCompressFailedToSave: (reason: String) -> String
}
