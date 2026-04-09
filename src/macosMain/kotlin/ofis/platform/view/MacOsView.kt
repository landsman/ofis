package ofis.platform.view

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import ofis.App
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationActivationPolicy
import platform.AppKit.NSImage
import platform.AppKit.NSOpenPanel
import platform.AppKit.NSSavePanel
import platform.AppKit.NSWindow
import platform.Foundation.NSBundle
import kotlin.coroutines.resume

actual fun platformGui() {
    val app = NSApplication.sharedApplication()
    app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)

    val icon = NSBundle.mainBundle.pathForResource("AppIcon", "icns")?.let { NSImage(contentsOfFile = it) }
        ?: NSImage(contentsOfFile = "build/mac_os_app_icon.png")
    icon?.let { app.setApplicationIconImage(it) }

    Window("Ofis") {
        App()
        LaunchedEffect(Unit) {
            app.activateIgnoringOtherApps(true)
            app.windows.forEach { window ->
                (window as? NSWindow)?.makeKeyAndOrderFront(null)
            }
        }
    }

    app.activateIgnoringOtherApps(true)
    app.run()
}

actual suspend fun saveFile(suggestedName: String): String? = withContext(Dispatchers.Main) {
    suspendCancellableCoroutine { continuation ->
        val panel = NSSavePanel.savePanel()
        panel.setNameFieldStringValue(suggestedName)
        panel.beginWithCompletionHandler { response ->
            continuation.resume(if (response == 1L) panel.URL()?.path else null)
        }
        panel.makeKeyAndOrderFront(null)
        NSApplication.sharedApplication().activateIgnoringOtherApps(true)
    }
}

actual suspend fun pickFile(allowedExtensions: List<String>): String? = withContext(Dispatchers.Main) {
    suspendCancellableCoroutine { continuation ->
        val panel = NSOpenPanel.openPanel()
        panel.setCanChooseFiles(true)
        panel.setCanChooseDirectories(false)
        panel.setAllowsMultipleSelection(false)
        // TODO: filter by allowedExtensions once SDK binding is clarified
        panel.beginWithCompletionHandler { response ->
            continuation.resume(if (response == 1L) panel.URL()?.path else null)
        }
        panel.makeKeyAndOrderFront(null)
        NSApplication.sharedApplication().activateIgnoringOtherApps(true)
    }
}
