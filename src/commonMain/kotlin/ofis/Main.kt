package ofis

import ofis.config.GlobalConfig
import ofis.config.Logger
import ofis.platform.defaultToGui
import ofis.platform.platformGui
import ofis.tool.ToolRegistry

fun commonMain(argList: List<String>): Int {
    val debugFlag = "--debug"
    val guiFlag = "--gui"
    val cliFlag = "--cli"
    val hasDebug = argList.contains(debugFlag)
    val hasGui = argList.contains(guiFlag)
    val hasCli = argList.contains(cliFlag)
    GlobalConfig.debug = hasDebug

    if (hasGui || (argList.isEmpty() && defaultToGui && !hasCli)) {
        platformGui()
        return 0
    }

    if (argList.isEmpty()) {
        println("Welcome to Ofis!")
        println("Available tools:")
        ToolRegistry.list().forEach { println("- ${it.name}: ${it.description}") }
        return 0
    }

    val filteredArgs = argList.filter { it != debugFlag && it != guiFlag && it != cliFlag }

    if (filteredArgs.isEmpty()) {
        println("Error: No tool specified.")
        return 1
    }

    val toolName = filteredArgs[0]
    val toolArgs = filteredArgs.drop(1)
    val tool = ToolRegistry.get(toolName)

    if (tool != null) {
        Logger.debug("Running tool: $toolName with args: $toolArgs")
        tool.run(toolArgs)
        return 0
    } else {
        println("Unknown tool: $toolName")
        return 1
    }
}
