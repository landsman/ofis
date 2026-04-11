package ofis.i18n

object EnStrings : AppStrings {
    // ── Common ────────────────────────────────────────────────────────────────
    override val appSubtitle = "Tooling Platform"
    override val back = "← Back"
    override val open = "Open"
    override val settings = "Settings"
    override val orDragAndDrop = "or drag and drop here"
    override val clearSelection = "Clear selection"

    // ── Logs ──────────────────────────────────────────────────────────────────
    override val logsTitle = "Logs"
    override val logsClose = "Close logs"
    override val logsEmpty = "No logs yet..."

    // ── PDF Compress ──────────────────────────────────────────────────────────
    override val pdfCompressName = "Compress PDF"
    override val pdfCompressDescription = "Reduce file size while keeping your document readable."
    override val pdfCompressTapToSelect = "Tap to select a PDF"
    override val pdfCompressDropHere = "Drop PDF here"
    override val pdfCompressCompressionLevel = "Compression Level"
    override val pdfCompressRecommended = "Recommended"
    override val pdfCompressProfileHighQuality = "High Quality"
    override val pdfCompressProfileHighQualityDesc = "Lossless. Preserves all images at full resolution."
    override val pdfCompressProfileBalanced = "Balanced"
    override val pdfCompressProfileBalancedDesc = "Recommended. Good quality, noticeably smaller file."
    override val pdfCompressProfileMaximum = "Maximum Compression"
    override val pdfCompressProfileMaximumDesc = "Smallest file. Images will be visibly lower quality."
    override val pdfCompressAlmostThere = "Almost there!"
    override val pdfCompressCompressing = "Compressing your PDF..."
    override val pdfCompressMakingSmaller = "Making your PDF file smaller..."
    override val pdfCompressComplete = "Compression Complete"
    override val pdfCompressAlreadyOptimal = "This PDF is already well-optimized. The compressed version isn't smaller."
    override val pdfCompressOriginal = "Original"
    override val pdfCompressCompressed = "Compressed"
    override val pdfCompressPercentSmaller = { n: Int -> "$n% smaller" }
    override val pdfCompressButton = "Compress PDF"
    override val pdfCompressSave = "Save compressed PDF"
    override val pdfCompressCompressAnother = "Compress another file"
    override val pdfCompressTryMaximum = "Try Maximum Compression"
    override val pdfCompressSaveCancelled = "Save cancelled."
    override val pdfCompressFileSaved = "File saved successfully."
    override val pdfCompressNotEnoughSpace = "Not enough disk space."
    override val pdfCompressPermissionDenied = "Permission denied."
    override val pdfCompressNotEnoughSpaceDetail = {
        need: String,
        avail: String,
        ->
        "Not enough disk space. Need $need, only $avail available."
    }
    override val pdfCompressFailedToSave = { reason: String -> "Failed to save: $reason" }

    // ── Settings ──────────────────────────────────────────────────────────────
    override val settingsTitle = "Settings"
    override val settingsLanguageLabel = "Language"
    override val settingsLanguageSystem = "System default"
    override val settingsLanguageEnglish = "English"
    override val settingsLanguageCzech = "Czech"
}
