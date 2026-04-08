package ofis

import kotlinx.cinterop.*
import platform.AppKit.*
import platform.Foundation.*
import platform.CoreGraphics.*
import platform.darwin.*
import platform.objc.*

object UIStyle {
    val backgroundColor = NSColor.colorWithCalibratedRed(0.98, 0.98, 0.98, 1.0)
    val dotColor = NSColor.colorWithCalibratedRed(0.85, 0.85, 0.85, 1.0)
    val cardColor = NSColor.whiteColor
    val cardBorderColor = NSColor.colorWithCalibratedRed(0.9, 0.9, 0.9, 1.0)
    val textColor = NSColor.labelColor
    val secondaryTextColor = NSColor.secondaryLabelColor
    
    val paddingLarge = 40.0
    val paddingMedium = 20.0
    val paddingSmall = 10.0
    val cornerRadius = 12.0
    
    val headlineSize = 42.0
    val subheadlineSize = 18.0
    val bodySize = 16.0
    val captionSize = 12.0
}

class VerticalStack(frame: CValue<NSRect>) : NSView(frame) {
    private var nextY: Double = 0.0
    private val spacing = UIStyle.paddingMedium

    init {
        setAutoresizingMask(NSViewWidthSizable or NSViewHeightSizable)
        // Since macOS uses bottom-left origin, we'll track y from top to bottom
        // but it's easier to start at the top (height) and subtract.
        nextY = frame.useContents { size.height } - UIStyle.paddingLarge
    }

    fun addElement(view: NSView, height: Double, customSpacing: Double? = null) {
        val currentSpacing = customSpacing ?: spacing
        val viewWidth = this.bounds.useContents { size.width }
        
        // Adjust nextY for the new element's height BEFORE setting frame
        nextY -= height
        
        view.setFrame(NSMakeRect(UIStyle.paddingLarge, nextY, viewWidth - (UIStyle.paddingLarge * 2), height))
        addSubview(view)
        
        // Prepare nextY for the NEXT element
        nextY -= currentSpacing
    }
    
    fun skip(amount: Double) {
        nextY -= amount
    }
}

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
        CGContextSetFillColorWithColor(context, UIStyle.backgroundColor.CGColor)
        CGContextFillRect(context, this.bounds)
        
        // Draw dots
        CGContextSetFillColorWithColor(context, UIStyle.dotColor.CGColor)
        
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
        
        val stack = VerticalStack(frame)
        addSubview(stack)

        // Ofis Headline
        val ofisLabel = NSTextField.labelWithString("Ofis")
        ofisLabel.setFont(NSFont.boldSystemFontOfSize(UIStyle.headlineSize))
        ofisLabel.setTextColor(UIStyle.textColor)
        stack.addElement(ofisLabel, 50.0, UIStyle.paddingSmall)
        
        val titleLabel = NSTextField.labelWithString("Tooling Platform")
        titleLabel.setFont(NSFont.systemFontOfSize(UIStyle.subheadlineSize))
        titleLabel.setTextColor(UIStyle.secondaryTextColor)
        stack.addElement(titleLabel, 25.0, UIStyle.paddingLarge)

        val tools = ToolRegistry.list()
        
        tools.forEach { tool ->
            val container = NSView(frame = NSMakeRect(0.0, 0.0, 100.0, 70.0)) // Stack will resize width
            container.setWantsLayer(true)
            container.layer?.setBackgroundColor(UIStyle.cardColor.CGColor)
            container.layer?.setCornerRadius(UIStyle.cornerRadius)
            container.layer?.setBorderWidth(1.0)
            container.layer?.setBorderColor(UIStyle.cardBorderColor.CGColor)
            
            val nameLabel = NSTextField.labelWithString(tool.name)
            nameLabel.setFont(NSFont.boldSystemFontOfSize(UIStyle.bodySize))
            nameLabel.setFrameOrigin(NSMakePoint(15.0, 35.0))
            container.addSubview(nameLabel)
            
            val descLabel = NSTextField.labelWithString(tool.description)
            descLabel.setFont(NSFont.systemFontOfSize(UIStyle.captionSize))
            descLabel.setTextColor(UIStyle.secondaryTextColor)
            descLabel.setFrameOrigin(NSMakePoint(15.0, 15.0))
            container.addSubview(descLabel)
            
            val button = NSButton(frame = NSMakeRect(0.0, 20.0, 100.0, 30.0))
            button.setTitle("Open")
            button.setBezelStyle(NSBezelStyleRounded)
            button.setAutoresizingMask(NSViewMinXMargin)
            
            // Re-calculate button X after width is known (container will be resized by stack)
            // But container width is NOT yet final in addElement's logic.
            // Let's use simple resizing mask or fixed width for button.
            button.setFrameOrigin(NSMakePoint(frame.useContents { size.width } - UIStyle.paddingLarge * 2 - 115.0, 20.0))
            
            val handler = ActionHandler().also { handlers.add(it) }
            handler.setOnAction { navigator.showTool(tool) }
            button.setTarget(handler)
            button.setAction(NSSelectorFromString("performAction"))
            
            container.addSubview(button)
            stack.addElement(container, 70.0)
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
        
        val stack = VerticalStack(frame)
        addSubview(stack)

        val headerContainer = NSView(frame = NSMakeRect(0.0, 0.0, 100.0, 40.0))
        
        val backButton = NSButton(frame = NSMakeRect(0.0, 5.0, 80.0, 30.0))
        backButton.setTitle("← Back")
        backButton.setBezelStyle(NSBezelStyleRounded)
        val backHandler = ActionHandler().also { handlers.add(it) }
        backHandler.setOnAction { navigator.showToolSelection() }
        backButton.setTarget(backHandler)
        backButton.setAction(NSSelectorFromString("performAction"))
        headerContainer.addSubview(backButton)

        val titleLabel = NSTextField.labelWithString(tool.name)
        titleLabel.setFont(NSFont.boldSystemFontOfSize(20.0))
        titleLabel.setFrameOrigin(NSMakePoint(90.0, 7.0))
        headerContainer.addSubview(titleLabel)
        
        stack.addElement(headerContainer, 40.0, UIStyle.paddingMedium)

        val scrollView = NSScrollView(frame = NSMakeRect(0.0, 0.0, 100.0, 300.0))
        scrollView.setHasVerticalScroller(true)
        scrollView.setBorderType(NSNoBorder)
        scrollView.setDrawsBackground(false)

        textView = NSTextView(frame = NSMakeRect(0.0, 0.0, 100.0, 300.0))
        textView.setEditable(false)
        textView.setRichText(false)
        textView.setDrawsBackground(false)
        textView.setAutoresizingMask(NSViewWidthSizable)

        scrollView.setDocumentView(textView)
        stack.addElement(scrollView, 260.0, UIStyle.paddingMedium)

        val actionButton = NSButton(frame = NSMakeRect(0.0, 0.0, 200.0, 40.0))
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
        
        val buttonContainer = NSView(frame = NSMakeRect(0.0, 0.0, 200.0, 40.0))
        buttonContainer.addSubview(actionButton)
        stack.addElement(buttonContainer, 40.0)
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
