package ofis.platform.service

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSystemFreeSize
import platform.Foundation.NSNumber
import platform.posix.X_OK
import platform.posix.access
import platform.posix.fgets
import platform.posix.pclose
import platform.posix.popen

actual fun availableDiskSpace(dirPath: String): Long {
    val attrs = NSFileManager.defaultManager.attributesOfFileSystemForPath(dirPath, error = null)
    val free = attrs?.get(NSFileSystemFreeSize) as? NSNumber
    return free?.longValue ?: Long.MAX_VALUE
}

@OptIn(ExperimentalForeignApi::class)
actual fun findHelperBinary(name: String): String? {
    // 1. Inside the .app bundle (Contents/MacOS/) — used in distributed DMG builds
    NSBundle.mainBundle.executablePath
        ?.substringBeforeLast("/")
        ?.takeIf { it.contains(".app/") }
        ?.let { dir ->
            val candidate = "$dir/$name"
            if (access(candidate, X_OK) == 0) return candidate
        }

    // 2. System PATH via `which` — works in `make dev` / CLI context
    val result = popen("which $name", "r")
    if (result != null) {
        val buf = ByteArray(256)
        buf.usePinned { pinned -> fgets(pinned.addressOf(0), buf.size, result) }
        pclose(result)
        val path =
            buf
                .decodeToString()
                .substringBefore('\u0000')
                .trim()
                .takeIf { it.isNotEmpty() && !it.startsWith("not found") }
        if (path != null) return path
    }

    // 3. Common Homebrew locations (fallback)
    return listOf(
        "/opt/homebrew/bin/$name", // Apple Silicon
        "/usr/local/bin/$name", // Intel
        "/usr/bin/$name",
    ).firstOrNull { access(it, X_OK) == 0 }
}
