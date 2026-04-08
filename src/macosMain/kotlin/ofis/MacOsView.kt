package ofis.platform

import androidx.compose.ui.window.Window
import ofis.App
import platform.AppKit.NSApplication
import platform.AppKit.NSOpenPanel

actual fun pickFile(allowedExtensions: List<String>): String? {
    val panel = NSOpenPanel.openPanel()
    panel.setCanChooseFiles(true)
    panel.setCanChooseDirectories(false)
    panel.setAllowsMultipleSelection(false)
    // TODO: filter by allowedExtensions once SDK binding is clarified

    return if (panel.runModal() == 1L /* NSModalResponseOK */) {
        panel.URL()?.path
    } else {
        null
    }
}

actual fun platformGui() {
    val app = NSApplication.sharedApplication()
    // Sets up a Compose-backed NSWindow (non-blocking)
    Window("Ofis") {
        App()
    }

    app.activateIgnoringOtherApps(true)
    app.run() // AppKit event loop — blocks until app quits
}
