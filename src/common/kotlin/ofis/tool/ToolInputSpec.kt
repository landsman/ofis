package ofis.tool

/**
 * Describes what file input a tool accepts.
 *
 * @param extensions Lowercase file extensions without dot, e.g. setOf("pdf", "pdf/a").
 * @param label      Human-readable type description, e.g. "PDF document".
 */
data class ToolInputSpec(
    val extensions: Set<String>,
    val label: String,
)
