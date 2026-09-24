package ofis

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ofis.config.Logger
import ofis.tool.Tool

internal enum class Screen { Tools, Settings, FileSuggestion }

internal class DroppedFile(
    val path: String,
    val size: Long?,
    val tools: List<Tool>,
)

/** Which screen [App] shows; a selected tool takes precedence over [screen]. */
internal class AppNavigator {
    var currentTool by mutableStateOf<Tool?>(null)
        private set
    var screen by mutableStateOf(Screen.Tools)
        private set
    var droppedFile by mutableStateOf<DroppedFile?>(null)
        private set

    fun openTool(
        tool: Tool,
        via: String = "",
    ) {
        Logger.debug("navigate → ${tool.name}$via")
        droppedFile = null
        screen = Screen.Tools
        currentTool = tool
    }

    fun closeTool() {
        Logger.debug("navigate → home")
        currentTool = null
    }

    fun showDroppedFile(file: DroppedFile) {
        droppedFile = file
        screen = Screen.FileSuggestion
    }

    fun dismissDroppedFile() {
        droppedFile = null
        screen = Screen.Tools
    }

    fun openSettings() {
        screen = Screen.Settings
    }

    fun home() {
        screen = Screen.Tools
    }
}
