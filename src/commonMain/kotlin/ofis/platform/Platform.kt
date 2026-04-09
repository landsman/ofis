package ofis.platform

import okio.FileSystem

expect val fileSystem: FileSystem

/** True on platforms where the app defaults to GUI mode when launched with no arguments (e.g. macOS .app bundle). */
expect val defaultToGui: Boolean

expect fun platformMain(args: List<String>)

expect fun platformGui()

expect fun exitProcess(status: Int)

expect suspend fun pickFile(allowedExtensions: List<String>): String?

expect suspend fun saveFile(suggestedName: String): String?

/** Returns available bytes on the volume containing [dirPath], or [Long.MAX_VALUE] if unknown. */
expect fun availableDiskSpace(dirPath: String): Long
