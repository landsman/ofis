package ofis.platform.view

actual fun platformGui() {
    println("GUI is not supported on Windows yet.")
}

actual suspend fun pickFile(allowedExtensions: List<String>): String? = null

actual suspend fun saveFile(suggestedName: String): String? = null
