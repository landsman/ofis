package ofis

import ofis.config.GlobalConfig
import ofis.config.Logger
import ofis.platform.defaultToGui
import ofis.platform.view.platformGui
import ofis.tool.ToolRegistry

private const val DEBUG_FLAG = "--debug"
private const val GUI_FLAG = "--gui"
private const val CLI_FLAG = "--cli"
private val FLAGS = setOf(DEBUG_FLAG, GUI_FLAG, CLI_FLAG)

fun commonMain(argList: List<String>): Int {
    GlobalConfig.debug = DEBUG_FLAG in argList
    val toolArgs = argList.filter { it !in FLAGS }

    return when {
        // GUI
        GUI_FLAG in argList || (argList.isEmpty() && defaultToGui) -> {
            platformGui()
            0
        }

        // CLI it is, let's offer the user the available tools
        argList.isEmpty() -> {
            println("Welcome to Ofis!")
            println("Available tools:")
            ToolRegistry.list().forEach { println("- ${it.name}: ${it.description}") }
            0
        }

        toolArgs.isEmpty() -> {
            println("Error: No tool specified.")
            1
        }

        // CLI: user has specified a tool, let's run it
        else -> runTool(toolArgs.first(), toolArgs.drop(1))
    }
}

private fun runTool(
    toolName: String,
    toolArgs: List<String>,
): Int {
    val tool = ToolRegistry.get(toolName)
    if (tool == null) {
        println("Unknown tool: $toolName")
        return 1
    }
    Logger.debug("Running tool: $toolName with args: $toolArgs")
    tool.run(toolArgs)
    return 0
}
