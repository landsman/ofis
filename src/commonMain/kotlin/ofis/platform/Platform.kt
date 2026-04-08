package ofis.platform

import okio.FileSystem

expect val fileSystem: FileSystem

expect fun platformMain(args: List<String>)

expect fun platformGui()

expect fun exitProcess(status: Int)

expect fun pickFile(allowedExtensions: List<String>): String?
