package ofis.tool.pdf.compress.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val saveMessages =
        remember(strings) {
            SaveMessages(
                saveCancelled = strings.pdfCompressSaveCancelled,
                fileSaved = strings.pdfCompressFileSaved,
                notEnoughSpace = strings.pdfCompressNotEnoughSpace,
                permissionDenied = strings.pdfCompressPermissionDenied,
                notEnoughSpaceDetail = strings.pdfCompressNotEnoughSpaceDetail,
                failedToSave = strings.pdfCompressFailedToSave,
            )
        }

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

    // Consume file drops from the AppKit drag-and-drop layer.
    LaunchedEffect(FileDropBus.pendingFilePath) {
        val path = FileDropBus.pendingFilePath ?: return@LaunchedEffect
        FileDropBus.consume()
        selectedFilePath = path
        selectedFileSize = fileSystem.metadataOrNull(path.toPath())?.size
        resizeInfo = null
        outputFilePath = null
        suggestedSaveName = null
        logController.clear()
    }

    // Parse structured messages that arrive via LogController
    DisposableEffect(logController) {
        val observer: (String) -> Unit = { msg ->
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
            }
        }
        logController.addObserver(observer)
        onDispose { logController.removeObserver(observer) }
    }

    val runCompression: (CompressionProfile) -> Unit = { profile ->
        selectedFilePath?.let { path ->
            selectedProfile = profile
            resizeInfo = null
            outputFilePath = null
            suggestedSaveName = null
            logController.clear()
            isRunning = true
            scope.launch {
                withContext(Dispatchers.Default) {
                    tool.run(listOf(path, "--profile", profile.toArg()))
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 40.dp, end = 40.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FileDropZone(
                selectedFilePath = selectedFilePath,
                selectedFileSize = selectedFileSize,
                placeholder = strings.pdfCompressTapToSelect,
                onSelect = {
                    scope.launch {
                        kotlinx.coroutines.yield()
                        pickFile(listOf("pdf"))?.let {
                            selectedFilePath = it
                            selectedFileSize = fileSystem.metadataOrNull(it.toPath())?.size
                            resizeInfo = null
                            logController.clear()
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

            // ── footer — action buttons ─────────────────────────────────────────
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
                            val result = saveCompressedFile(outputFilePath!!, suggestedSaveName, saveMessages)
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
                    logController.clear()
                })
            }
        }

        // Full-screen drop overlay — appears whenever a file drag enters the window.
        if (FileDropBus.isDragging) {
            DropOverlay()
        }
    }
}

@Composable
private fun DropOverlay() {
    val strings = LocalAppStrings.current
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xEEF5FBFF), RoundedCornerShape(12.dp))
                .border(3.dp, Color(0xFF4A90E2), RoundedCornerShape(12.dp))
                .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "📄", fontSize = 72.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = strings.pdfCompressDropHere,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2A70C2),
            )
        }
    }
}
