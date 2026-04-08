package ofis

import platform.AppKit.*
import platform.Foundation.*
import platform.objc.*
import kotlinx.cinterop.*
import platform.darwin.*

class ActionHandler : NSObject() {
    private var onSelect: (() -> Unit)? = null
    
    fun setOnSelect(block: () -> Unit) {
        onSelect = block
    }

    @ObjCAction
    fun performSelect() {
        onSelect?.invoke()
    }
}

// Keep delegates alive for the lifetime of the app to avoid GC
private var globalHandler: ActionHandler? = null
private var globalAppDelegate: AppDelegate? = null
private var globalWindowDelegate: WindowDelegate? = null
private var appName = "Ofis"

class AppDelegate : NSObject(), NSApplicationDelegateProtocol {
    override fun applicationShouldTerminateAfterLastWindowClosed(sender: NSApplication): Boolean = true
}

class WindowDelegate : NSObject(), NSWindowDelegateProtocol {
    override fun windowWillClose(notification: NSNotification) {
        // Ensure the process terminates when the window is closed
        NSApplication.sharedApplication().terminate(null)
    }
}

private fun setAppIcon() {
    // Load from generated PNG
    val loadedImage = NSImage(contentsOfFile = "AppIcon.png")
    if (loadedImage != null) {
        NSApplication.sharedApplication().setApplicationIconImage(loadedImage)
    } else {
        Logger.info("Error: AppIcon.png not found. Please run 'make generate-icon' to convert the SVG icon.")
    }
}

actual fun platformGui() {
    autoreleasepool {
        val app = NSApplication.sharedApplication()
        
        // Set the application name programmatically
        NSProcessInfo.processInfo().setProcessName(appName)
        
        app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)

        setAppIcon()

        val window = NSWindow(
            contentRect = NSMakeRect(0.0, 0.0, 600.0, 400.0),
            styleMask = NSWindowStyleMaskTitled or NSWindowStyleMaskClosable or NSWindowStyleMaskMiniaturizable or NSWindowStyleMaskResizable,
            backing = NSBackingStoreBuffered,
            defer = false
        )
        window.setTitle(appName)

        // Set up app/window delegates so closing the window terminates the app (and the make task)
        val appDelegate = AppDelegate().also { globalAppDelegate = it }
        app.setDelegate(appDelegate)
        val windowDelegate = WindowDelegate().also { globalWindowDelegate = it }
        window.setDelegate(windowDelegate)

        val scrollView = NSScrollView(frame = NSMakeRect(20.0, 70.0, 560.0, 300.0))
        scrollView.setHasVerticalScroller(true)
        scrollView.setAutoresizingMask(NSViewWidthSizable or NSViewHeightSizable)
        
        val textView = NSTextView(frame = NSMakeRect(0.0, 0.0, 560.0, 300.0))
        textView.setEditable(false)
        textView.setRichText(false)
        textView.setAutoresizingMask(NSViewWidthSizable)
        
        scrollView.setDocumentView(textView)
        window.contentView?.addSubview(scrollView)

        val button = NSButton(frame = NSMakeRect(20.0, 20.0, 150.0, 32.0))
        button.setTitle("Select & Compress")
        button.setBezelStyle(NSBezelStyleRounded)
        window.contentView?.addSubview(button)

        val handler = ActionHandler()
        globalHandler = handler // Keep it alive
        handler.setOnSelect {
            val openPanel = NSOpenPanel.openPanel()
            openPanel.setCanChooseFiles(true)
            openPanel.setCanChooseDirectories(false)
            openPanel.setAllowsMultipleSelection(false)
            openPanel.setAllowedFileTypes(listOf("pdf"))

            if (openPanel.runModal() == NSModalResponseOK) {
                val url = openPanel.URLs.firstOrNull() as? NSURL
                val path = url?.path
                if (path != null) {
                    textView.setString("") // Clear logs
                    Logger.onLog = { msg ->
                        dispatch_async(dispatch_get_main_queue()) {
                            val current = textView.string
                            textView.setString(current + msg + "\n")
                            textView.scrollToEndOfDocument(null)
                        }
                    }
                    
                    GlobalConfig.debug = true
                    Logger.info("Selected file: $path")
                    
                    val tool = ToolRegistry.get("pdf-compress")
                    if (tool != null) {
                        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0UL)) {
                            tool.run(listOf(path))
                        }
                    } else {
                        Logger.info("Error: pdf-compress tool not found")
                    }
                }
            }
        }
        
        button.setTarget(handler)
        button.setAction(NSSelectorFromString("performSelect"))

        window.center()
        window.makeKeyAndOrderFront(null)
        app.run()
    }
}
