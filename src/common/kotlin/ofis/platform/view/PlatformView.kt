package ofis.platform.view

/** Launches the native GUI event loop for the current platform.
 *  Blocks until the application window is closed. */
expect fun platformGui()

/** Opens a native file picker dialog and returns the selected file path, or null if cancelled.
 *  [allowedExtensions] hints the platform to filter visible files (e.g. ["pdf"]), but support varies by platform. */
expect suspend fun pickFile(allowedExtensions: List<String>): String?

/** Opens a native save dialog pre-filled with [suggestedName] and returns the chosen destination path,
 *  or null if the user canceled. */
expect suspend fun saveFile(suggestedName: String): String?
