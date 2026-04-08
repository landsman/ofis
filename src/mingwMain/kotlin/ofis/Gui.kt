package ofis.platform


actual fun platformGui() {
    println("GUI is not supported on Windows yet.")
}

actual fun getGuiNavigator(): GuiNavigator = throw UnsupportedOperationException("GUI is not supported on Windows yet.")

actual fun pickFile(allowedExtensions: List<String>): String? = null
