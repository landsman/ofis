package ofis.platform

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import kotlinx.coroutines.suspendCancellableCoroutine
import ofis.App
import platform.AppKit.NSApplication
import platform.AppKit.NSFloatingWindowLevel
import platform.AppKit.NSImage
import platform.AppKit.NSOpenPanel
import platform.AppKit.NSWindow
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSystemFreeSize
import platform.Foundation.NSNumber
import kotlin.coroutines.resume

actual fun availableDiskSpace(dirPath: String): Long {
    val attrs = NSFileManager.defaultManager.attributesOfFileSystemForPath(dirPath, error = null)
    val free = attrs?.get(NSFileSystemFreeSize) as? NSNumber
    return free?.longValue ?: Long.MAX_VALUE
}

actual suspend fun saveFile(suggestedName: String): String? = suspendCancellableCoroutine { continuation ->
    val panel = platform.AppKit.NSSavePanel.savePanel()
    panel.setNameFieldStringValue(suggestedName)
    panel.beginWithCompletionHandler { response ->
        if (response == 1L /* NSModalResponseOK */) {
            continuation.resume(panel.URL()?.path)
        } else {
            continuation.resume(null)
        }
    }
}

actual suspend fun pickFile(allowedExtensions: List<String>): String? = suspendCancellableCoroutine { continuation ->
    val panel = NSOpenPanel.openPanel()
    panel.setCanChooseFiles(true)
    panel.setCanChooseDirectories(false)
    panel.setAllowsMultipleSelection(false)
    // TODO: filter by allowedExtensions once SDK binding is clarified

    panel.beginWithCompletionHandler { response ->
        if (response == 1L /* NSModalResponseOK */) {
            continuation.resume(panel.URL()?.path)
        } else {
            continuation.resume(null)
        }
    }
}

actual fun platformGui() {
    val app = NSApplication.sharedApplication()

    // Set dock icon from generated PNG (produced by `make icon`)
    NSImage(contentsOfFile = "build/mac_os_app_icon.png").let {
        app.setApplicationIconImage(it)
    }

    // Sets up a Compose-backed NSWindow (non-blocking)
    Window("Ofis") {
        App()
        LaunchedEffect(Unit) {
            app.activateIgnoringOtherApps(true)
            app.windows.forEach { window ->
                (window as? NSWindow)?.makeKeyAndOrderFront(null)
                (window as? NSWindow)?.setLevel(NSFloatingWindowLevel)
            }
        }
    }

    app.activateIgnoringOtherApps(true)
    app.run() // AppKit event loop — blocks until app quits
}
