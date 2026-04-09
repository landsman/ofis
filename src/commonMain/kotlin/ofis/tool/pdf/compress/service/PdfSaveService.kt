package ofis.tool.pdf.compress.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ofis.platform.fileSystem
import ofis.platform.service.availableDiskSpace
import ofis.platform.view.saveFile
import ofis.ui.system.ToastData
import ofis.utils.format.formatSize
import okio.Path.Companion.toPath

data class SaveResult(val savedPath: String? = null, val toast: ToastData)

suspend fun saveCompressedFile(outputFilePath: String, suggestedSaveName: String?): SaveResult {
    val dest = saveFile(suggestedSaveName ?: "compressed.pdf")
        ?: return SaveResult(toast = ToastData("Save cancelled.", isSuccess = false))

    return withContext(Dispatchers.Default) {
        val srcPath = outputFilePath.toPath()
        val destPath = dest.toPath()
        val destDir = destPath.parent?.toString() ?: "/"

        val fileSize = fileSystem.metadataOrNull(srcPath)?.size ?: 0L
        val freeSpace = availableDiskSpace(destDir)
        if (freeSpace < fileSize) {
            return@withContext SaveResult(
                toast = ToastData(
                    "Not enough disk space. Need ${formatSize(fileSize)}, only ${formatSize(freeSpace)} available.",
                    isSuccess = false
                )
            )
        }

        try {
            try {
                fileSystem.atomicMove(srcPath, destPath)
            } catch (_: Exception) {
                fileSystem.copy(srcPath, destPath)
                fileSystem.delete(srcPath)
            }
            SaveResult(savedPath = dest, toast = ToastData("File saved successfully.", isSuccess = true))
        } catch (e: Exception) {
            val reason = when {
                e.message?.contains("No space left", ignoreCase = true) == true ||
                e.message?.contains("ENOSPC", ignoreCase = true) == true -> "Not enough disk space."
                e.message?.contains("Permission", ignoreCase = true) == true -> "Permission denied."
                else -> e.message ?: "Unknown error."
            }
            SaveResult(toast = ToastData("Failed to save: $reason", isSuccess = false))
        }
    }
}
