package ofis

import okio.FileSystem

interface Tool {
    val name: String
    val description: String

    fun run(args: List<String>)
}

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

expect val fileSystem: FileSystem

expect fun platformMain(args: List<String>)

expect fun platformGui()

interface GuiNavigator {
    fun showToolSelection()
    fun showTool(tool: Tool)
}

expect fun getGuiNavigator(): GuiNavigator

expect fun exitProcess(status: Int)

object GlobalConfig {
    var debug: Boolean = false
}

object Logger {
    var onLog: ((String) -> Unit)? = null

    fun info(msg: String) {
        println(msg)
        onLog?.invoke(msg)
    }

    fun debug(msg: String) {
        if (GlobalConfig.debug) {
            println("[DEBUG] $msg")
            onLog?.invoke("[DEBUG] $msg")
        }
    }
}

fun commonMain(argList: List<String>) {
    val debugFlag = "--debug"
    val hasDebug = argList.contains(debugFlag)
    GlobalConfig.debug = hasDebug

    val guiFlag = "--gui"
    if (argList.contains(guiFlag)) {
        platformGui()
        return
    }

    if (argList.isEmpty()) {
        println("Welcome to Ofis - Multiplatform Tooling")
        println("Available tools:")
        ToolRegistry.list().forEach { println("- ${it.name}: ${it.description}") }
        return
    }

    val filteredArgs = argList.filter { it != debugFlag && it != guiFlag }

    if (filteredArgs.isEmpty()) {
        println("Error: No tool specified.")
        exitProcess(1)
        return
    }

    val toolName = filteredArgs[0]
    val toolArgs = filteredArgs.drop(1)
    val tool = ToolRegistry.get(toolName)

    if (tool != null) {
        Logger.debug("Running tool: $toolName with args: $toolArgs")
        tool.run(toolArgs)
    } else {
        println("Unknown tool: $toolName")
        exitProcess(1)
    }
}
