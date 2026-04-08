package ofis.platform

import ofis.tool.Tool
import okio.FileSystem

expect val fileSystem: FileSystem

expect fun platformMain(args: List<String>)

expect fun platformGui()

interface GuiNavigator {
    fun showToolSelection()
    fun showTool(tool: Tool)
}

expect fun getGuiNavigator(): GuiNavigator

expect fun exitProcess(status: Int)

expect fun pickFile(allowedExtensions: List<String>): String?
