package ofis.platform.service

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.toKString
import platform.posix.X_OK
import platform.posix.access
import platform.posix.fgets
import platform.posix.getenv
import platform.posix.pclose
import platform.posix.popen

actual fun availableDiskSpace(dirPath: String): Long = Long.MAX_VALUE // TODO: statvfs

@OptIn(ExperimentalForeignApi::class)
actual fun findHelperBinary(name: String): String? {
    // 1. Next to the executable (for self-contained Linux packages)
    getenv("_")
        ?.toKString()
        ?.substringBeforeLast("/")
        ?.takeIf { it.isNotEmpty() }
        ?.let { dir ->
            val candidate = "$dir/$name"
            if (access(candidate, X_OK) == 0) return candidate
        }

    // 2. System PATH via `which`
    val result = popen("which $name", "r")
    if (result != null) {
        val buf = ByteArray(256)
        buf.usePinned { pinned -> fgets(pinned.addressOf(0), buf.size, result) }
        pclose(result)
        val path = buf.decodeToString().trim().takeIf { it.isNotEmpty() && !it.startsWith("not found") }
        if (path != null) return path
    }

    // 3. Common Linux locations
    return listOf("/usr/bin/$name", "/usr/local/bin/$name").firstOrNull {
        access(it, X_OK) == 0
    }
}
