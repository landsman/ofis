package ofis.tool.pdf.compress.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ofis.i18n.AppStrings
import ofis.i18n.LocalAppStrings
import ofis.platform.fileSystem
import ofis.platform.view.pickFile
import ofis.tool.Tool
import ofis.tool.pdf.compress.model.CompressionProfile
import ofis.tool.pdf.compress.service.SaveMessages
import ofis.tool.pdf.compress.service.saveCompressedFile
import ofis.ui.system.FileDropBus
import ofis.ui.system.FileDropZone
import ofis.ui.system.LocalLogController
import ofis.ui.system.toast.LocalToastController
import okio.Path.Companion.toPath

@Composable
fun PdfDetailScreen(tool: Tool) {
    val scope = rememberCoroutineScope()
    val toastController = LocalToastController.current
    val logController = LocalLogController.current
    val strings = LocalAppStrings.current
    val saveMessages = remember(strings) { strings.toSaveMessages() }
    val state = remember { PdfDetailState() }

    CompressionEvents(state)

    PdfDetailContent(
        state = state,
        placeholder = strings.pdfCompressTapToSelect,
        onPick = {
            scope.launch {
                kotlinx.coroutines.yield()
                pickFile(listOf("pdf"))?.let {
                    state.selectFile(it, fileSystem.metadataOrNull(it.toPath())?.size)
                    logController.clear()
                }
            }
        },
        onCompress = { profile ->
            state.selectedFilePath?.let { path ->
                state.startCompression(profile)
                logController.clear()
                scope.launch {
                    withContext(Dispatchers.Default) {
                        tool.run(listOf(path, "--profile", profile.toArg()))
                    }
                }
            }
        },
        onSave = { outputPath ->
            scope.launch {
                kotlinx.coroutines.yield()
                val result = saveCompressedFile(outputPath, state.suggestedSaveName, saveMessages)
                withContext(Dispatchers.Main) {
                    result.savedPath?.let { state.outputFilePath = it }
                    toastController.show(result.toast)
                }
            }
        },
        onReset = {
            state.clearFile()
            logController.clear()
        },
    )
}

/** Feeds dropped files and the tool's structured log lines into [state]. */
@Composable
private fun CompressionEvents(state: PdfDetailState) {
    val scope = rememberCoroutineScope()
    val toastController = LocalToastController.current
    val logController = LocalLogController.current

    // Consume file drops from the AppKit drag-and-drop layer.
    LaunchedEffect(FileDropBus.pendingFilePath) {
        val path = FileDropBus.pendingFilePath ?: return@LaunchedEffect
        FileDropBus.consume()
        state.selectFile(path, fileSystem.metadataOrNull(path.toPath())?.size)
        logController.clear()
    }

    DisposableEffect(logController) {
        val observer: (String) -> Unit = { msg ->
            scope.launch(Dispatchers.Main) {
                state.onLogMessage(msg)?.let { toastController.show(it, isSuccess = false) }
            }
        }
        logController.addObserver(observer)
        onDispose { logController.removeObserver(observer) }
    }
}

@Composable
private fun PdfDetailContent(
    state: PdfDetailState,
    placeholder: String,
    onPick: () -> Unit,
    onCompress: (CompressionProfile) -> Unit,
    onSave: (outputPath: String) -> Unit,
    onReset: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 40.dp, end = 40.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FileDropZone(
                selectedFilePath = state.selectedFilePath,
                selectedFileSize = state.selectedFileSize,
                placeholder = placeholder,
                onSelect = onPick,
                onClear = { state.clearFile() },
            )

            if (state.showProfileSelector) {
                Spacer(modifier = Modifier.height(20.dp))
                ProfileSelector(selected = state.selectedProfile, onSelect = { state.selectedProfile = it })
            }

            if (state.isRunning) {
                Spacer(modifier = Modifier.height(16.dp))
                CompressionProgress()
            }

            state.resizeInfo?.let {
                Spacer(modifier = Modifier.height(16.dp))
                CompressionResultCard(info = it, profile = state.completedProfile)
            }

            Spacer(modifier = Modifier.height(16.dp))
            PdfActions(state, onCompress, onSave, onReset)
        }
    }
}

/** Footer — action buttons. */
@Composable
private fun PdfActions(
    state: PdfDetailState,
    onCompress: (CompressionProfile) -> Unit,
    onSave: (outputPath: String) -> Unit,
    onReset: () -> Unit,
) {
    if (state.resizeInfo == null) {
        CompressPdfSubmitButton(
            enabled = state.selectedFilePath != null && !state.isRunning,
            onClick = { onCompress(state.selectedProfile) },
        )
        return
    }
    val outputPath = state.outputFilePath
    if (state.isAlreadyOptimal && state.selectedProfile != CompressionProfile.MAXIMUM) {
        TryHigherCompressionButton(onClick = { onCompress(CompressionProfile.MAXIMUM) })
        Spacer(modifier = Modifier.height(10.dp))
    } else if (outputPath != null) {
        SaveButton(onClick = { onSave(outputPath) })
        Spacer(modifier = Modifier.height(10.dp))
    }
    CompressAnotherPdfFileButton(onClick = onReset)
}

private fun AppStrings.toSaveMessages() =
    SaveMessages(
        saveCancelled = pdfCompressSaveCancelled,
        fileSaved = pdfCompressFileSaved,
        notEnoughSpace = pdfCompressNotEnoughSpace,
        permissionDenied = pdfCompressPermissionDenied,
        notEnoughSpaceDetail = pdfCompressNotEnoughSpaceDetail,
        failedToSave = pdfCompressFailedToSave,
    )
