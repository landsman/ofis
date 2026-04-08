package ofis

import okio.FileSystem

import platform.posix.exit

actual val fileSystem: FileSystem = FileSystem.SYSTEM

actual fun platformMain(args: List<String>) {
    commonMain(args)
}

actual fun exitProcess(status: Int) {
    exit(status)
}

fun main(args: Array<String>) {
    platformMain(args.toList())
}
