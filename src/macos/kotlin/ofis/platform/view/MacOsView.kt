package ofis.platform.view

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import ofis.App
import ofis.ui.system.FileDropBus
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationActivationPolicy
import platform.AppKit.NSApplicationDelegateProtocol
import platform.AppKit.NSDragOperationCopy
import platform.AppKit.NSDraggingInfoProtocol
import platform.AppKit.NSFilenamesPboardType
import platform.AppKit.NSImage
import platform.AppKit.NSOpenPanel
import platform.AppKit.NSSavePanel
import platform.AppKit.NSView
import platform.AppKit.NSViewHeightSizable
import platform.AppKit.NSViewWidthSizable
import platform.AppKit.NSWorkspace
import platform.CoreGraphics.CGRect
import platform.Foundation.NSBundle
import platform.Foundation.NSMakeRect
import platform.Foundation.NSURL
import platform.darwin.NSObject
import kotlin.coroutines.resume

private class AppDelegate :
    NSObject(),
    NSApplicationDelegateProtocol {
    override fun applicationShouldTerminateAfterLastWindowClosed(sender: NSApplication): Boolean = true
}

/** Transparent full-window NSView that captures file drag-and-drop from Finder.
 *  Placed as the outermost contentView so mouse events still reach the Compose
 *  view (a subview at the same size) via the default responder chain. */
private class FileDragDropView(
    frame: CValue<CGRect>,
    private val onDragEnter: () -> Unit,
    private val onDragExit: () -> Unit,
    private val onFileDrop: (String) -> Unit,
) : NSView(frame) {
    init {
        @Suppress("UNCHECKED_CAST")
        registerForDraggedTypes(listOf(NSFilenamesPboardType) as List<*>)
    }

    override fun draggingEntered(sender: NSDraggingInfoProtocol): ULong {
        onDragEnter()
        return NSDragOperationCopy
    }

    override fun draggingUpdated(sender: NSDraggingInfoProtocol): ULong = NSDragOperationCopy

    override fun draggingExited(sender: NSDraggingInfoProtocol?) {
        onDragExit()
    }

    override fun performDragOperation(sender: NSDraggingInfoProtocol): Boolean {
        @Suppress("UNCHECKED_CAST")
        val files =
            sender.draggingPasteboard
                .propertyListForType(NSFilenamesPboardType) as? List<String>
        val path = files?.firstOrNull() ?: return false
        onFileDrop(path)
        return true
    }
}

actual fun platformGui() {
    val app = NSApplication.sharedApplication()
    app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)
    app.delegate = AppDelegate()

    val icon =
        NSBundle.mainBundle.pathForResource("AppIcon", "icns")?.let { NSImage(contentsOfFile = it) }
            ?: NSImage(contentsOfFile = "build/mac_os_app_icon.png")
    icon.let { app.setApplicationIconImage(it) }

    Window("Ofis") {
        App()
        LaunchedEffect(Unit) {
            app.activateIgnoringOtherApps(true)

            window.makeKeyAndOrderFront(null)
            window.acceptsMouseMovedEvents = true

            // Wrap the Compose contentView inside a FileDragDropView so Finder
            // file-drops are forwarded to FileDropBus. Mouse/keyboard events
            // still reach the Compose view because it is a subview (hit-tested first).
            window.contentView?.let { composeView ->
                val frame =
                    composeView.frame.useContents {
                        NSMakeRect(origin.x, origin.y, size.width, size.height)
                    }
                val dragView =
                    FileDragDropView(
                        frame = frame,
                        onDragEnter = { FileDropBus.onDragEnter() },
                        onDragExit = { FileDropBus.onDragExit() },
                        onFileDrop = { path -> FileDropBus.onDrop(path) },
                    )
                window.contentView = dragView
                dragView.addSubview(composeView)
                // Keep the Compose view filling the drag wrapper on every resize.
                composeView.autoresizingMask = NSViewWidthSizable or NSViewHeightSizable
                composeView.setFrame(dragView.bounds)
            }
        }
    }

    app.activateIgnoringOtherApps(true)
    app.run()
}

actual suspend fun saveFile(suggestedName: String): String? =
    withContext(Dispatchers.Main) {
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

actual fun openUrl(url: String) {
    NSURL.URLWithString(url)?.let { NSWorkspace.sharedWorkspace.openURL(it) }
}

actual suspend fun pickFile(allowedExtensions: List<String>): String? =
    withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            val panel = NSOpenPanel.openPanel()
            panel.setCanChooseFiles(true)
            panel.setCanChooseDirectories(false)
            panel.setAllowsMultipleSelection(false)
            panel.beginWithCompletionHandler { response ->
                continuation.resume(if (response == 1L) panel.URL()?.path else null)
            }
            panel.makeKeyAndOrderFront(null)
            NSApplication.sharedApplication().activateIgnoringOtherApps(true)
        }
    }
