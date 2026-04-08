package ofis.tool
import androidx.compose.runtime.Composable

interface Tool {
    /** CLI key used for invocation, e.g. "pdf-compress". */
    val name: String

    /** Human-readable label shown in the UI, e.g. "Compress PDF". */
    val displayName: String

    /** Short description shown in tool cards. */
    val description: String

    /** Main screen for this tool. */
    @Composable
    fun Screen(onBack: () -> Unit)

    fun register()

    fun run(args: List<String>)
}
