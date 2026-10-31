package ofis.tool.image.png.optimize.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ofis.i18n.LocalAppStrings
import ofis.platform.fileSystem
import ofis.platform.view.pickFile
import ofis.tool.Tool
import ofis.tool.pdf.compress.service.saveCompressedFile
import ofis.tool.pdf.compress.ui.CompressAnotherPdfFileButton
import ofis.tool.pdf.compress.ui.CompressPdfSubmitButton
import ofis.tool.pdf.compress.ui.CompressionEvents
import ofis.tool.pdf.compress.ui.CompressionProgress
import ofis.tool.pdf.compress.ui.CompressionResultCard
import ofis.tool.pdf.compress.ui.PdfDetailState
import ofis.tool.pdf.compress.ui.SaveButton
import ofis.tool.pdf.compress.ui.toSaveMessages
import ofis.ui.system.FileDropZone
import ofis.ui.system.LocalLogController
import ofis.ui.system.toast.LocalToastController
import okio.Path.Companion.toPath

/** The PDF screen's flow without its profile selector: oxipng has one lossless mode. */
@Composable
fun PngDetailScreen(tool: Tool) {
    val scope = rememberCoroutineScope()
    val logController = LocalLogController.current
    val strings = LocalAppStrings.current
    val state = remember { PdfDetailState() }

    CompressionEvents(state)

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
            placeholder = strings.pngOptimizeTapToSelect,
            onSelect = {
                scope.launch {
                    kotlinx.coroutines.yield()
                    pickFile(listOf("png"))?.let {
                        state.selectFile(it, fileSystem.metadataOrNull(it.toPath())?.size)
                        logController.clear()
                    }
                }
            },
            onClear = { state.clearFile() },
        )

        if (state.isRunning) {
            Spacer(modifier = Modifier.height(16.dp))
            CompressionProgress(making = strings.pngOptimizeMakingSmaller, working = strings.pngOptimizeOptimizing)
        }

        state.resizeInfo?.let {
            Spacer(modifier = Modifier.height(16.dp))
            CompressionResultCard(info = it, alreadyOptimalText = strings.pngOptimizeAlreadyOptimal)
        }

        Spacer(modifier = Modifier.height(16.dp))

        PngActions(state, tool)
    }
}

/** Footer: optimize until there is a result, then save it or start over. */
@Composable
private fun PngActions(
    state: PdfDetailState,
    tool: Tool,
) {
    val scope = rememberCoroutineScope()
    val toastController = LocalToastController.current
    val logController = LocalLogController.current
    val strings = LocalAppStrings.current

    if (state.resizeInfo == null) {
        CompressPdfSubmitButton(
            enabled = state.selectedFilePath != null && !state.isRunning,
            label = strings.pngOptimizeButton,
            onClick = {
                state.selectedFilePath?.let { path ->
                    state.startCompression(state.selectedProfile)
                    logController.clear()
                    scope.launch(Dispatchers.Default) { tool.run(listOf(path)) }
                }
            },
        )
        return
    }
    state.outputFilePath?.let { outputPath ->
        SaveButton(
            label = strings.pngOptimizeSave,
            onClick = {
                scope.launch {
                    kotlinx.coroutines.yield()
                    val result = saveCompressedFile(outputPath, state.suggestedSaveName, strings.toSaveMessages())
                    withContext(Dispatchers.Main) {
                        result.savedPath?.let { state.outputFilePath = it }
                        toastController.show(result.toast)
                    }
                }
            },
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
    CompressAnotherPdfFileButton(
        onClick = {
            state.clearFile()
            logController.clear()
        },
    )
}
