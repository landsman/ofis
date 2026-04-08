package ofis

import kotlinx.cinterop.*
import platform.AppKit.*
import platform.Foundation.*
import platform.CoreGraphics.*
import platform.darwin.*
import platform.objc.*

class ActionHandler : NSObject() {
    private var onAction: (() -> Unit)? = null

    fun setOnAction(block: () -> Unit) {
        onAction = block
    }

    @ObjCAction
    fun performAction() {
        onAction?.invoke()
    }
}

class MacOSNavigator(private val window: NSWindow) : GuiNavigator {
    override fun showToolSelection() {
        dispatch_async(dispatch_get_main_queue()) {
            val selectionView = ToolSelectionView(window.contentRectForFrameRect(window.frame), this@MacOSNavigator)
            window.setContentView(selectionView)
            window.setTitle("Ofis - Select Tool")
        }
    }

    override fun showTool(tool: Tool) {
        dispatch_async(dispatch_get_main_queue()) {
            val toolView = ToolDetailView(window.contentRectForFrameRect(window.frame), tool, this@MacOSNavigator)
            window.setContentView(toolView)
            window.setTitle("Ofis - ${tool.name}")
        }
    }
}

class DotGridView(frame: CValue<NSRect>) : NSView(frame) {
    override fun drawRect(dirtyRect: CValue<NSRect>) {
        super.drawRect(dirtyRect)
        val context = NSGraphicsContext.currentContext()?.CGContext ?: return
        
        // Fill background
        CGContextSetFillColorWithColor(context, NSColor.colorWithCalibratedRed(0.98, 0.98, 0.98, 1.0).CGColor)
        CGContextFillRect(context, this.bounds)
        
        // Draw dots
        val dotColor = NSColor.colorWithCalibratedRed(0.85, 0.85, 0.85, 1.0).CGColor
        CGContextSetFillColorWithColor(context, dotColor)
        
        val spacing = 20.0
        val dotSize = 2.0 // Slightly larger for visibility
        
        val bounds = this.bounds
        bounds.useContents {
            var x = 0.0
            while (x < size.width) {
                var y = 0.0
                while (y < size.height) {
                    CGContextFillEllipseInRect(context, NSMakeRect(x, y, dotSize, dotSize))
                    y += spacing
                }
                x += spacing
            }
        }
    }
    
    override fun isOpaque(): Boolean = true
}

class ToolSelectionView(frame: CValue<NSRect>, private val navigator: GuiNavigator) : NSView(frame) {
    private val handlers = mutableListOf<ActionHandler>()

    init {
        val bg = DotGridView(frame)
        bg.setAutoresizingMask(NSViewWidthSizable or NSViewHeightSizable)
        addSubview(bg)
        
        val viewHeight = frame.useContents { size.height }
        val viewWidth = frame.useContents { size.width }

        // Ofis Headline
        val ofisLabel = NSTextField.labelWithString("Ofis")
        ofisLabel.setFont(NSFont.boldSystemFontOfSize(42.0))
        ofisLabel.setTextColor(NSColor.labelColor)
        ofisLabel.setFrameOrigin(NSMakePoint(40.0, viewHeight - 80.0))
        addSubview(ofisLabel)
        
        val titleLabel = NSTextField.labelWithString("Tooling Platform")
        titleLabel.setFont(NSFont.systemFontOfSize(18.0))
        titleLabel.setTextColor(NSColor.secondaryLabelColor)
        titleLabel.setFrameOrigin(NSMakePoint(40.0, viewHeight - 110.0))
        addSubview(titleLabel)

        val tools = ToolRegistry.list()
        var yOffset = viewHeight - 180.0
        
        tools.forEach { tool ->
            val container = NSView(frame = NSMakeRect(40.0, yOffset, viewWidth - 80.0, 60.0))
            container.setWantsLayer(true)
            container.layer?.setBackgroundColor(NSColor.whiteColor.CGColor)
            container.layer?.setCornerRadius(12.0)
            container.layer?.setBorderWidth(1.0)
            container.layer?.setBorderColor(NSColor.colorWithCalibratedRed(0.9, 0.9, 0.9, 1.0).CGColor)
            container.setAutoresizingMask(NSViewWidthSizable)
            
            val nameLabel = NSTextField.labelWithString(tool.name)
            nameLabel.setFont(NSFont.boldSystemFontOfSize(16.0))
            nameLabel.setFrameOrigin(NSMakePoint(15.0, 30.0))
            container.addSubview(nameLabel)
            
            val descLabel = NSTextField.labelWithString(tool.description)
            descLabel.setFont(NSFont.systemFontOfSize(12.0))
            descLabel.setTextColor(NSColor.secondaryLabelColor)
            descLabel.setFrameOrigin(NSMakePoint(15.0, 10.0))
            container.addSubview(descLabel)
            
            val button = NSButton(frame = NSMakeRect(container.frame.useContents { size.width } - 115.0, 15.0, 100.0, 30.0))
            button.setTitle("Open")
            button.setBezelStyle(NSBezelStyleRounded)
            button.setAutoresizingMask(NSViewMinXMargin)
            
            val handler = ActionHandler().also { handlers.add(it) }
            handler.setOnAction { navigator.showTool(tool) }
            button.setTarget(handler)
            button.setAction(NSSelectorFromString("performAction"))
            
            container.addSubview(button)
            addSubview(container)
            yOffset -= 70.0
        }
    }
}

class ToolDetailView(frame: CValue<NSRect>, private val tool: Tool, private val navigator: GuiNavigator) : NSView(frame) {
    private val handlers = mutableListOf<ActionHandler>()
    private val textView: NSTextView

    init {
        val bg = DotGridView(frame)
        bg.setAutoresizingMask(NSViewWidthSizable or NSViewHeightSizable)
        addSubview(bg)
        
        val viewHeight = frame.useContents { size.height }

        val backButton = NSButton(frame = NSMakeRect(20.0, viewHeight - 40.0, 80.0, 30.0))
        backButton.setTitle("← Back")
        backButton.setBezelStyle(NSBezelStyleRounded)
        val backHandler = ActionHandler().also { handlers.add(it) }
        backHandler.setOnAction { navigator.showToolSelection() }
        backButton.setTarget(backHandler)
        backButton.setAction(NSSelectorFromString("performAction"))
        addSubview(backButton)

        val titleLabel = NSTextField.labelWithString(tool.name)
        titleLabel.setFont(NSFont.boldSystemFontOfSize(20.0))
        titleLabel.setFrameOrigin(NSMakePoint(110.0, viewHeight - 38.0))
        addSubview(titleLabel)

        val scrollView = NSScrollView(frame = NSMakeRect(20.0, 80.0, 560.0, 260.0))
        scrollView.setHasVerticalScroller(true)
        scrollView.setAutoresizingMask(NSViewWidthSizable or NSViewHeightSizable)

        textView = NSTextView(frame = NSMakeRect(0.0, 0.0, 560.0, 260.0))
        textView.setEditable(false)
        textView.setRichText(false)
        textView.setDrawsBackground(false)
        textView.setAutoresizingMask(NSViewWidthSizable)

        scrollView.setDocumentView(textView)
        scrollView.setDrawsBackground(false)
        addSubview(scrollView)

        val actionButton = NSButton(frame = NSMakeRect(20.0, 20.0, 200.0, 40.0))
        actionButton.setTitle("Select File & Run")
        actionButton.setBezelStyle(NSBezelStyleRounded)
        
        val runHandler = ActionHandler().also { handlers.add(it) }
        runHandler.setOnAction {
            val openPanel = NSOpenPanel.openPanel()
            openPanel.setCanChooseFiles(true)
            openPanel.setCanChooseDirectories(false)
            openPanel.setAllowsMultipleSelection(false)
            if (tool.name.contains("pdf", ignoreCase = true)) {
                openPanel.setAllowedFileTypes(listOf("pdf"))
            }

            if (openPanel.runModal() == NSModalResponseOK) {
                val url = openPanel.URLs.firstOrNull() as? NSURL
                val path = url?.path
                if (path != null) {
                    textView.setString("")
                    Logger.onLog = { msg ->
                        dispatch_async(dispatch_get_main_queue()) {
                            val current = textView.string
                            textView.setString(current + msg + "\n")
                            textView.scrollToEndOfDocument(null)
                        }
                    }
                    GlobalConfig.debug = true
                    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0UL)) {
                        tool.run(listOf(path))
                    }
                }
            }
        }
        actionButton.setTarget(runHandler)
        actionButton.setAction(NSSelectorFromString("performAction"))
        addSubview(actionButton)
    }
}

private var globalNavigator: MacOSNavigator? = null
private var globalAppDelegate: AppDelegate? = null
private var globalWindowDelegate: WindowDelegate? = null
private var appName = "Ofis"

class AppDelegate : NSObject(), NSApplicationDelegateProtocol {
    override fun applicationShouldTerminateAfterLastWindowClosed(sender: NSApplication): Boolean = true
}

class WindowDelegate : NSObject(), NSWindowDelegateProtocol {
    override fun windowWillClose(notification: NSNotification) {
        NSApplication.sharedApplication().terminate(null)
    }
}

private fun setAppIcon() {
    val iconPath = "build/mac_os_app_icon.png"
    if (NSFileManager.defaultManager.fileExistsAtPath(iconPath)) {
        val loadedImage = NSImage(contentsOfFile = iconPath)
        NSApplication.sharedApplication().setApplicationIconImage(loadedImage)
    }
}

actual fun getGuiNavigator(): GuiNavigator {
    return globalNavigator ?: throw IllegalStateException("Navigator not initialized. Call platformGui() first.")
}

actual fun platformGui() {
    autoreleasepool {
        val app = NSApplication.sharedApplication()
        NSProcessInfo.processInfo().setProcessName(appName)
        app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)
        setAppIcon()

        val window = NSWindow(
            contentRect = NSMakeRect(0.0, 0.0, 600.0, 450.0),
            styleMask = NSWindowStyleMaskTitled or NSWindowStyleMaskClosable or NSWindowStyleMaskMiniaturizable or NSWindowStyleMaskResizable,
            backing = NSBackingStoreBuffered,
            defer = false,
        )
        window.setTitle(appName)

        val appDelegate = AppDelegate().also { globalAppDelegate = it }
        app.setDelegate(appDelegate)
        val windowDelegate = WindowDelegate().also { globalWindowDelegate = it }
        window.setDelegate(windowDelegate)

        val navigator = MacOSNavigator(window).also { globalNavigator = it }
        navigator.showToolSelection()

        window.center()
        window.makeKeyAndOrderFront(null)
        app.run()
    }
}
