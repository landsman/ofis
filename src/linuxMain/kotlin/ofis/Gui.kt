package ofis

actual fun platformGui() {
    println("GUI is not supported on Linux yet.")
}

actual fun getGuiNavigator(): GuiNavigator = throw UnsupportedOperationException("GUI is not supported on Linux yet.")
