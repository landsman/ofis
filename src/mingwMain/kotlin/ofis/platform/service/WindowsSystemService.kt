package ofis.platform.service

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.fgets
import platform.posix.pclose
import platform.posix.popen

actual fun availableDiskSpace(dirPath: String): Long = Long.MAX_VALUE // TODO: GetDiskFreeSpaceEx

@OptIn(ExperimentalForeignApi::class)
actual fun findHelperBinary(name: String): String? {
    // 1. Next to the executable (for self-contained Windows builds)
    // TODO: implement via GetModuleFileNameW when needed

    // 2. System PATH via `where`
    val result = popen("where $name", "r")
    if (result != null) {
        val buf = ByteArray(512)
        buf.usePinned { pinned -> fgets(pinned.addressOf(0), buf.size, result) }
        pclose(result)
        val path = buf.decodeToString().trim().takeIf { it.isNotEmpty() && !it.startsWith("INFO:") }
        if (path != null) return path
    }

    return null
}
