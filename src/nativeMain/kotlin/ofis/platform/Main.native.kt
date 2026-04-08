package ofis.platform

import ofis.commonMain
import okio.FileSystem
import platform.posix.exit

actual val fileSystem: FileSystem = FileSystem.SYSTEM

actual fun platformMain(args: List<String>) {
    commonMain(args)
}

actual fun platformGui() {}

actual fun exitProcess(status: Int) {
    exit(status)
}

actual suspend fun pickFile(allowedExtensions: List<String>): String? = null

actual suspend fun saveFile(suggestedName: String): String? = null

actual fun availableDiskSpace(dirPath: String): Long = Long.MAX_VALUE

fun main(args: Array<String>) {
    platformMain(args.toList())
}
