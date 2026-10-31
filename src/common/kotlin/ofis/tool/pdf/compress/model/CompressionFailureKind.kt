package ofis.tool.pdf.compress.model

/** What the user is told about a failed compression. The name travels on the `FAILURE_KIND:` log line. */
enum class CompressionFailureKind {
    /** A PDF tool rejected the file, which is usually damaged, password-protected or not a PDF. */
    UNREADABLE_FILE,

    FILE_NOT_FOUND,

    /** qpdf or Ghostscript is neither bundled nor on PATH, or could not be launched. */
    TOOL_MISSING,

    NOT_ENOUGH_SPACE,

    CANNOT_WRITE,

    UNEXPECTED,
}
