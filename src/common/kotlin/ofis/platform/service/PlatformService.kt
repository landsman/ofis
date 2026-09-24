package ofis.platform.service

/** Returns the platform-appropriate directory for persistent app data.
 *  - macOS:   ~/Library/Application Support/Ofis
 *  - Linux:   ~/.local/share/ofis  (or $XDG_DATA_HOME/ofis)
 *  - Windows: %APPDATA%\Ofis
 *  The directory is guaranteed to exist when returned. */
expect fun appDataDir(): String

/** Returns the number of free bytes on the volume containing [dirPath].
 *  Used before writing output files to guard against out-of-disk-space failures.
 *  Returns [Long.MAX_VALUE] if the platform cannot determine free space. */
expect fun availableDiskSpace(dirPath: String): Long

/** Resolves a helper binary (e.g. "qpdf", "gs") to its absolute path.
 *  Each platform searches in its own preferred order:
 *  - macOS: .app bundle → PATH (`which`) → Homebrew fallback paths
 *  - Linux:  executable directory → PATH (`which`) → /usr/bin, /usr/local/bin
 *  - Windows: executable directory → PATH (`where`)
 *  Returns null if the binary cannot be found, which should surface as a
 *  [ofis.tool.pdf.compress.model.CompressionError.BinaryNotFound] error. */
expect fun findHelperBinary(name: String): String?
