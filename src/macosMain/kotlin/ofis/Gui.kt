package ofis

import androidx.compose.ui.window.Window
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationDelegateProtocol
import platform.darwin.NSObject

actual fun getGuiNavigator(): GuiNavigator = object : GuiNavigator {
    override fun showToolSelection() {}
    override fun showTool(tool: Tool) {}
}

actual fun platformGui() {
    val app = NSApplication.sharedApplication()
    Window(
        title = "Ofis"
    ) {
        App()
    }
    app.run()
}
