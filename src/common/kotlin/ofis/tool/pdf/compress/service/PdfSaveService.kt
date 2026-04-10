package ofis.tool.pdf.compress.service

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ofis.generated.resources.Res
import ofis.generated.resources.failed_to_save
import ofis.generated.resources.file_saved
import ofis.generated.resources.not_enough_space
import ofis.generated.resources.not_enough_space_detail
import ofis.generated.resources.permission_denied
import ofis.generated.resources.save_cancelled
import ofis.platform.fileSystem
import ofis.platform.service.availableDiskSpace
import ofis.platform.view.saveFile
import ofis.ui.system.toast.ToastData
import ofis.utils.format.formatSize
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.stringResource

data class SaveMessages(
    val saveCancelled: String,
    val fileSaved: String,
    val notEnoughSpace: String,
    val permissionDenied: String,
    val notEnoughSpaceDetail: (need: String, avail: String) -> String,
    val failedToSave: (reason: String) -> String,
)

@Composable
fun rememberSaveMessages(): SaveMessages {
    val saveCancelledStr = stringResource(Res.string.save_cancelled)
    val fileSavedStr = stringResource(Res.string.file_saved)
    val notEnoughSpaceStr = stringResource(Res.string.not_enough_space)
    val permissionDeniedStr = stringResource(Res.string.permission_denied)
    val notEnoughSpaceDetailTemplate = stringResource(Res.string.not_enough_space_detail)
    val failedToSaveTemplate = stringResource(Res.string.failed_to_save)
    return remember(
        saveCancelledStr,
        fileSavedStr,
        notEnoughSpaceStr,
        permissionDeniedStr,
        notEnoughSpaceDetailTemplate,
        failedToSaveTemplate,
    ) {
        SaveMessages(
            saveCancelled = saveCancelledStr,
            fileSaved = fileSavedStr,
            notEnoughSpace = notEnoughSpaceStr,
            permissionDenied = permissionDeniedStr,
            notEnoughSpaceDetail = { need, avail ->
                notEnoughSpaceDetailTemplate
                        .replace("{need}", need)
                        .replace("{avail}", avail)
            },
            failedToSave = { reason -> failedToSaveTemplate.replace("{reason}", reason) },
        )
    }
}

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
        } catch (e: Exception) {
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
