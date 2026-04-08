package ofis.platform

import androidx.compose.ui.window.Window
import ofis.App
import platform.AppKit.NSApplication
import platform.AppKit.NSImage
import platform.AppKit.NSOpenPanel
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSystemFreeSize
import platform.Foundation.NSNumber

actual fun availableDiskSpace(dirPath: String): Long {
    val attrs = NSFileManager.defaultManager.attributesOfFileSystemForPath(dirPath, error = null)
    val free = attrs?.get(NSFileSystemFreeSize) as? NSNumber
    return free?.longValue ?: Long.MAX_VALUE
}

actual fun saveFile(suggestedName: String): String? {
    val panel = platform.AppKit.NSSavePanel.savePanel()
    panel.setNameFieldStringValue(suggestedName)
    return if (panel.runModal() == 1L /* NSModalResponseOK */) panel.URL()?.path else null
}

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

    // Set dock icon from generated PNG (produced by `make icon`)
    NSImage(contentsOfFile = "build/mac_os_app_icon.png")?.let {
        app.setApplicationIconImage(it)
    }

    // Sets up a Compose-backed NSWindow (non-blocking)
    Window("Ofis") { App() }

    app.activateIgnoringOtherApps(true)
    app.run() // AppKit event loop — blocks until app quits
}
