package ofis.platform.service

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import platform.posix.X_OK
import platform.posix.access
import platform.posix.fgets
import platform.posix.getenv
import platform.posix.mkdir
import platform.posix.pclose
import platform.posix.popen

private const val DIR_MODE_0755 = 0b111101101u

actual fun appDataDir(): String {
    val xdg = getenv("XDG_DATA_HOME")?.toKString()?.takeIf { it.isNotEmpty() }
    val home = getenv("HOME")?.toKString() ?: "."
    val base = xdg ?: "$home/.local/share"
    val dir = "$base/ofis"
    mkdir(dir, DIR_MODE_0755)
    return dir
}

/** Not measured on Linux yet (statvfs); reporting unlimited skips the pre-write space check. */
actual fun availableDiskSpace(dirPath: String): Long = Long.MAX_VALUE

actual fun findHelperBinary(name: String): String? =
    // 1. Next to the executable (for self-contained Linux packages)
    bundledBinary(name)
        // 2. System PATH via `which`
        ?: whichBinary(name)
        // 3. Common Linux locations
        ?: listOf("/usr/bin/$name", "/usr/local/bin/$name").firstOrNull { access(it, X_OK) == 0 }

@OptIn(ExperimentalForeignApi::class)
private fun bundledBinary(name: String): String? =
    getenv("_")
        ?.toKString()
        ?.substringBeforeLast("/")
        ?.takeIf { it.isNotEmpty() }
        ?.let { dir -> "$dir/$name" }
        ?.takeIf { access(it, X_OK) == 0 }

@OptIn(ExperimentalForeignApi::class)
private fun whichBinary(name: String): String? {
    val result = popen("which $name", "r") ?: return null
    val buf = ByteArray(256)
    buf.usePinned { pinned -> fgets(pinned.addressOf(0), buf.size, result) }
    pclose(result)
    return buf.decodeToString().trim().takeIf { it.isNotEmpty() && !it.startsWith("not found") }
}
