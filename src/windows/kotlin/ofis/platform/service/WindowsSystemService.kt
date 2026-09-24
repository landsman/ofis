package ofis.platform.service

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import platform.posix._pclose
import platform.posix._popen
import platform.posix.fgets
import platform.posix.getenv

actual fun appDataDir(): String {
    val appData = getenv("APPDATA")?.toKString()?.takeIf { it.isNotEmpty() } ?: "."
    val dir = "$appData\\Ofis"
    // CreateDirectoryW — ignore error if already exists
    _popen("mkdir \"$dir\" 2>nul", "r")?.let { _pclose(it) }
    return dir
}

/** Not measured on Windows yet (GetDiskFreeSpaceEx); reporting unlimited skips the pre-write space check. */
actual fun availableDiskSpace(dirPath: String): Long = Long.MAX_VALUE

@OptIn(ExperimentalForeignApi::class)
actual fun findHelperBinary(name: String): String? {
    // Only PATH is searched: a binary next to the executable (GetModuleFileNameW) is not supported yet.
    // System PATH via `where` — Windows equivalent of `which`
    val result = _popen("where $name", "r")
    if (result != null) {
        val buf = ByteArray(512)
        buf.usePinned { pinned ->
            fgets(pinned.addressOf(0), buf.size, result)
        }
        _pclose(result)
        val path = buf.decodeToString().trim().takeIf { it.isNotEmpty() && !it.startsWith("INFO:") }
        if (path != null) return path
    }

    return null
}
