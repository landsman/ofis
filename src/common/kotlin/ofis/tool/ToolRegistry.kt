package ofis.tool
import ofis.tool.pdf.compress.PdfCompressor

object ToolRegistry {
    private val tools = mutableMapOf<String, Tool>()
    private var initialized = false

    private fun ensureInitialized() {
        if (!initialized) {
            PdfCompressor().register()
            initialized = true
        }
    }

    fun register(tool: Tool) {
        tools[tool.name] = tool
    }

    fun get(name: String): Tool? {
        ensureInitialized()
        return tools[name]
    }

    fun list(): List<Tool> {
        ensureInitialized()
        return tools.values.toList()
    }
}
