package ofis.platform


actual fun platformGui() {
    println("GUI is not supported on Linux yet.")
}

actual fun pickFile(allowedExtensions: List<String>): String? = null

actual fun saveFile(suggestedName: String): String? = null
