package ofis.tool

interface Tool {
    /** CLI key used for invocation, e.g. "pdf-compress". */
    val name: String

    /** Human-readable label shown in the UI, e.g. "Compress PDF". */
    val displayName: String

    /** Short description shown in tool cards. */
    val description: String

    fun run(args: List<String>)
}
