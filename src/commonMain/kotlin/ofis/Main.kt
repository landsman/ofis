package ofis

import ofis.config.GlobalConfig
import ofis.config.Logger
import ofis.platform.exitProcess
import ofis.platform.platformGui

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
        println("Welcome to Ofis!")
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
