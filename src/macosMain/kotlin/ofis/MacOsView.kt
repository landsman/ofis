package ofis.platform

import androidx.compose.ui.window.Window
import kotlinx.cinterop.BetaInteropApi
import ofis.App
import ofis.tool.Tool
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationActivationPolicy
import platform.AppKit.NSImage
import platform.AppKit.NSOpenPanel
import platform.AppKit.NSApplicationDelegateProtocol
import platform.darwin.NSObject

actual fun getGuiNavigator(): GuiNavigator = object : GuiNavigator {
    override fun showToolSelection() {}
    override fun showTool(tool: Tool) {}
}

actual fun pickFile(allowedExtensions: List<String>): String? {
    val panel = NSOpenPanel.openPanel()
    panel.setCanChooseFiles(true)
    panel.setCanChooseDirectories(false)
    panel.setAllowsMultipleSelection(false)

    return if (panel.runModal() == 1L /* NSModalResponseOK */) {
        val url = panel.URL()
        url?.path
    } else {
        null
    }
}

@OptIn(BetaInteropApi::class)
@Suppress("CONFLICTING_OVERLOADS")
class AppDelegate : NSObject(), NSApplicationDelegateProtocol {
    override fun applicationShouldTerminateAfterLastWindowClosed(sender: NSApplication): Boolean {
        return true
    }
}

private var delegate: AppDelegate? = null

actual fun platformGui() {
    println("Launching GUI...")
    val app = NSApplication.sharedApplication()
    delegate = AppDelegate()
    app.delegate = delegate
    app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)

    val iconPath = "build/mac_os_app_icon.png"
    val icon = NSImage(byReferencingFile = iconPath)
    if (icon.isValid()) {
        app.setApplicationIconImage(icon)
    }

    Window(title = "Ofis") {
        App()
    }
    app.activateIgnoringOtherApps(true)
    app.run()
}
