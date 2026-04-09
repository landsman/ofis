package ofis.tool.pdf.compress.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ofis.config.Logger
import ofis.platform.fileSystem
import ofis.platform.view.pickFile
import ofis.tool.Tool
import ofis.tool.pdf.compress.model.CompressionProfile
import ofis.tool.pdf.compress.service.saveCompressedFile
import ofis.ui.system.FileDropZone
import ofis.ui.system.LogOverlay
import ofis.ui.system.toast.LocalToastController
import okio.Path.Companion.toPath

@Composable
fun PdfDetailScreen(
    tool: Tool,
    showLogs: Boolean,
    onCloseLogs: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val toastController = LocalToastController.current

    var logs by remember { mutableStateOf("") }
    var selectedFilePath by remember { mutableStateOf<String?>(null) }
    var selectedFileSize by remember { mutableStateOf<Long?>(null) }
    var resizeInfo by remember { mutableStateOf<String?>(null) }
    var completedProfile by remember { mutableStateOf<CompressionProfile?>(null) }
    var outputFilePath by remember { mutableStateOf<String?>(null) }
    var suggestedSaveName by remember { mutableStateOf<String?>(null) }
    var isRunning by remember { mutableStateOf(false) }
    var selectedProfile by remember { mutableStateOf(CompressionProfile.BALANCED) }

    val isAlreadyOptimal =
        resizeInfo
            ?.substringAfter("(")
            ?.substringBefore("%")
            ?.toIntOrNull()
            ?.let { it <= 0 } ?: false

    DisposableEffect(tool) {
        val listener: (String) -> Unit = { msg ->
            scope.launch(Dispatchers.Main) {
                when {
                    msg.startsWith("RESIZE_INFO: ") -> {
                        resizeInfo = msg.substringAfter("RESIZE_INFO: ")
                        completedProfile = selectedProfile
                        isRunning = false
                    }
                    msg.startsWith("OUTPUT_PATH: ") -> outputFilePath = msg.substringAfter("OUTPUT_PATH: ")
                    msg.startsWith("SUGGESTED_NAME: ") -> suggestedSaveName = msg.substringAfter("SUGGESTED_NAME: ")
                    msg.startsWith("Error: ") -> {
                        isRunning = false
                        toastController.show(msg.substringAfter("Error: "), isSuccess = false)
                    }
                }
                logs += msg + "\n"
            }
        }
        Logger.onLog = listener
        onDispose { if (Logger.onLog == listener) Logger.onLog = null }
    }

    val runCompression: (CompressionProfile) -> Unit = { profile ->
        selectedFilePath?.let { path ->
            selectedProfile = profile
            resizeInfo = null
            outputFilePath = null
            suggestedSaveName = null
            logs = ""
            isRunning = true
            scope.launch {
                withContext(Dispatchers.Default) {
                    tool.run(listOf(path, "--profile", profile.toArg()))
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── main content ───────────────────────────────────────────────────────
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FileDropZone(
                        selectedFilePath = selectedFilePath,
                        selectedFileSize = selectedFileSize,
                        placeholder = "Tap to select a PDF",
                        onSelect = {
                            scope.launch {
                                kotlinx.coroutines.yield()
                                pickFile(listOf("pdf"))?.let {
                                    selectedFilePath = it
                                    selectedFileSize = fileSystem.metadataOrNull(it.toPath())?.size
                                    resizeInfo = null
                                    logs = ""
                                }
                            }
                        },
                        onClear = {
                            selectedFilePath = null
                            selectedFileSize = null
                            resizeInfo = null
                            outputFilePath = null
                            suggestedSaveName = null
                        },
                    )

                    if (selectedFilePath != null && resizeInfo == null && !isRunning) {
                        Spacer(modifier = Modifier.height(20.dp))
                        ProfileSelector(selected = selectedProfile, onSelect = { selectedProfile = it })
                    }

                    if (isRunning) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CompressionProgress()
                    }

                    resizeInfo?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        CompressionResultCard(info = it, profile = completedProfile)
                    }
                }
            }

            // ── footer — action buttons ────────────────────────────────────────
            Spacer(modifier = Modifier.height(16.dp))
            if (resizeInfo == null) {
                CompressPdfSubmitButton(
                    enabled = selectedFilePath != null && !isRunning,
                    onClick = { runCompression(selectedProfile) },
                )
            } else {
                if (isAlreadyOptimal && selectedProfile != CompressionProfile.MAXIMUM) {
                    TryHigherCompressionButton(onClick = { runCompression(CompressionProfile.MAXIMUM) })
                    Spacer(modifier = Modifier.height(10.dp))
                } else if (outputFilePath != null) {
                    SaveButton(onClick = {
                        scope.launch {
                            kotlinx.coroutines.yield()
                            val result = saveCompressedFile(outputFilePath!!, suggestedSaveName)
                            withContext(Dispatchers.Main) {
                                result.savedPath?.let { outputFilePath = it }
                                toastController.show(result.toast)
                            }
                        }
                    })
                    Spacer(modifier = Modifier.height(10.dp))
                }
                CompressAnotherPdfFileButton(onClick = {
                    resizeInfo = null
                    selectedFilePath = null
                    selectedFileSize = null
                    outputFilePath = null
                    suggestedSaveName = null
                    logs = ""
                })
            }
        }

        // ── log overlay — sits on top of everything ────────────────────────────
        if (showLogs) {
            LogOverlay(logs = logs, onClose = onCloseLogs)
        }
    }
}
