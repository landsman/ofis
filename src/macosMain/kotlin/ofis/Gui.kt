package ofis

import androidx.compose.ui.window.Window
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationActivationPolicy

actual fun getGuiNavigator(): GuiNavigator = object : GuiNavigator {
    override fun showToolSelection() {}
    override fun showTool(tool: Tool) {}
}

actual fun platformGui() {
    val app = NSApplication.sharedApplication()
    app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)
    Window(title = "Ofis") {
        App()
    }
    app.activateIgnoringOtherApps(true)
    app.run()
}
