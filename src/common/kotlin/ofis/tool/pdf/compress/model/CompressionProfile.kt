package ofis.tool.pdf.compress.model

enum class CompressionProfile(
    val label: String,
    val description: String,
) {
    /** qpdf only — lossless stream recompression, no image downsampling */
    HIGH_QUALITY("High Quality", "Lossless. Preserves all images at full resolution."),

    /** qpdf + Ghostscript /ebook — moderate image downsampling to 144 DPI */
    BALANCED("Balanced", "Recommended. Good quality, noticeably smaller file."),

    /** qpdf + Ghostscript /screen — aggressive downsampling to 96 DPI */
    MAXIMUM("Maximum Compression", "Smallest file. Images will be visibly lower quality."),
    ;

    /** CLI argument used when passing this profile to the tool runner. */
    fun toArg(): String =
        when (this) {
            HIGH_QUALITY -> "high"
            BALANCED -> "balanced"
            MAXIMUM -> "max"
        }
}
