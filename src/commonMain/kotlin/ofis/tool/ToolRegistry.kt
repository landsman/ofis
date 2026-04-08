package ofis

import ofis.tool.Tool
import ofis.tool.pdf.compress.registerPdfTool

object ToolRegistry {
    private val tools = mutableMapOf<String, Tool>()
    private var initialized = false

    private fun ensureInitialized() {
        if (!initialized) {
            registerPdfTool()
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
