package ofis.platform

import okio.FileSystem

/** Filesystem access for the current platform. Used for reading/writing files in tools and services. */
expect val fileSystem: FileSystem

/** True on platforms where the app defaults to GUI mode when launched with no arguments (e.g. macOS .app bundle).
 *  False on CLI-first platforms (Linux, Windows) where the user must explicitly pass `--gui`. */
expect val defaultToGui: Boolean

/** Entry point called by the platform-specific `main()`. Delegates to [ofis.commonMain] with the parsed args. */
expect fun platformMain(args: List<String>)

/** Terminates the process with the given [status] code. Wraps the platform's native exit mechanism. */
expect fun exitProcess(status: Int)
