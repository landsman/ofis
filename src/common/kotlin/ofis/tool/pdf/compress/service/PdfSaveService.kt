package ofis.tool.pdf.compress.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ofis.platform.fileSystem
import ofis.platform.service.availableDiskSpace
import ofis.platform.view.saveFile
import ofis.ui.system.toast.ToastData
import ofis.utils.format.formatSize
import okio.IOException
import okio.Path.Companion.toPath

data class SaveMessages(
    val saveCancelled: String,
    val fileSaved: String,
    val notEnoughSpace: String,
    val permissionDenied: String,
    val notEnoughSpaceDetail: (need: String, avail: String) -> String,
    val failedToSave: (reason: String) -> String,
)

data class SaveResult(
    val savedPath: String? = null,
    val toast: ToastData,
)

suspend fun saveCompressedFile(
    outputFilePath: String,
    suggestedSaveName: String?,
    messages: SaveMessages,
): SaveResult {
    val dest =
        saveFile(suggestedSaveName ?: "compressed.pdf")
            ?: return SaveResult(toast = ToastData(messages.saveCancelled, isSuccess = false))

    return withContext(Dispatchers.Default) {
        val srcPath = outputFilePath.toPath()
        val destPath = dest.toPath()
        val destDir = destPath.parent?.toString() ?: "/"

        val fileSize = fileSystem.metadataOrNull(srcPath)?.size ?: 0L
        val freeSpace = availableDiskSpace(destDir)
        if (freeSpace < fileSize) {
            return@withContext SaveResult(
                toast =
                    ToastData(
                        messages.notEnoughSpaceDetail(formatSize(fileSize), formatSize(freeSpace)),
                        isSuccess = false,
                    ),
            )
        }

        try {
            try {
                fileSystem.atomicMove(srcPath, destPath)
            } catch (_: Exception) {
                fileSystem.copy(srcPath, destPath)
                fileSystem.delete(srcPath)
            }
            SaveResult(savedPath = dest, toast = ToastData(messages.fileSaved, isSuccess = true))
        } catch (e: IOException) {
            val reason =
                when {
                    e.message?.contains("No space left", ignoreCase = true) == true ||
                        e.message?.contains("ENOSPC", ignoreCase = true) == true -> messages.notEnoughSpace

                    e.message?.contains("Permission", ignoreCase = true) == true -> messages.permissionDenied

                    else -> e.message ?: "Unknown error."
                }
            SaveResult(toast = ToastData(messages.failedToSave(reason), isSuccess = false))
        }
    }
}
