package ofis.storage

import ofis.config.Logger
import ofis.platform.fileSystem
import ofis.platform.service.appDataDir
import okio.Path.Companion.toPath

/**
 * Persistent key-value storage backed by plain text files in the platform data directory.
 *
 * Each key maps to a single file: `<appDataDir>/<key>.txt`.
 * Values are plain UTF-8 strings. Missing keys return null.
 */
object AppStorage {
    private val dir: String by lazy { appDataDir() }

    fun read(key: String): String? =
        try {
            fileSystem.read("$dir/$key.txt".toPath()) { readUtf8() }.trim().takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            Logger.debug("AppStorage: read($key) failed — ${e.message}")
            null
        }

    fun write(
        key: String,
        value: String,
    ) {
        try {
            fileSystem.write("$dir/$key.txt".toPath()) { writeUtf8(value) }
            Logger.debug("AppStorage: write($key) → $dir/$key.txt")
        } catch (e: Exception) {
            Logger.error("AppStorage: write($key) failed — ${e.message}")
        }
    }
}
