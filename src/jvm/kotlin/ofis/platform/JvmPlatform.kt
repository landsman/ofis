package ofis.platform

import okio.FileSystem

actual val fileSystem: FileSystem = FileSystem.SYSTEM

actual val defaultToGui: Boolean = false

actual fun platformMain(args: List<String>): Unit = throw UnsupportedOperationException("JVM target is test-only")

actual fun exitProcess(status: Int): Unit = kotlin.system.exitProcess(status)
