package ofis.platform

import platform.AppKit.*
import platform.CoreGraphics.CGRectMake

actual fun pickFile(allowedExtensions: List<String>): String? {
    val panel = NSOpenPanel.openPanel()
    panel.setCanChooseFiles(true)
    panel.setCanChooseDirectories(false)
    panel.setAllowsMultipleSelection(false)
    panel.setAllowedFileTypes(allowedExtensions)

    return if (panel.runModal() == 1L /* NSModalResponseOK */) {
        val url = panel.URL()
        url?.path
    } else {
        null
    }
}

actual fun platformGui() {
    val app = NSApplication.sharedApplication()

    val styleMask = (NSWindowStyleMaskTitled or NSWindowStyleMaskClosable or NSWindowStyleMaskResizable)
    val window = NSWindow(
        contentRect = CGRectMake(0.0, 0.0, 1000.0, 700.0),
        styleMask = styleMask,
        backing = NSBackingStoreBuffered,
        defer = false
    )
    window.title = "Ofis"

    window.center()
    window.makeKeyAndOrderFront(null)
    app.activateIgnoringOtherApps(true)
    app.run()
}
