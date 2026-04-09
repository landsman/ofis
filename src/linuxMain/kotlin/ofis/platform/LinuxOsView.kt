package ofis.platform

actual val defaultToGui: Boolean = false

actual fun availableDiskSpace(dirPath: String): Long = Long.MAX_VALUE // TODO: statvfs

actual fun platformGui() {
    println("GUI is not supported on Linux yet.")
}

actual suspend fun pickFile(allowedExtensions: List<String>): String? = null

actual suspend fun saveFile(suggestedName: String): String? = null
