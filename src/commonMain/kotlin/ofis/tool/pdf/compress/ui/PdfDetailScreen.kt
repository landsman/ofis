package ofis.tool.pdf.compress.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import ofis.ui.system.LogView
import ofis.ui.system.ToastData
import ofis.ui.system.ToastHost
import okio.Path.Companion.toPath

@Composable
fun PdfDetailScreen(tool: Tool, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()

    var logs by remember { mutableStateOf("") }
    var showLogs by remember { mutableStateOf(false) }
    var selectedFilePath by remember { mutableStateOf<String?>(null) }
    var selectedFileSize by remember { mutableStateOf<Long?>(null) }
    var resizeInfo by remember { mutableStateOf<String?>(null) }
    var outputFilePath by remember { mutableStateOf<String?>(null) }
    var suggestedSaveName by remember { mutableStateOf<String?>(null) }
    var isRunning by remember { mutableStateOf(false) }
    var selectedProfile by remember { mutableStateOf(CompressionProfile.BALANCED) }
    var toast by remember { mutableStateOf<ToastData?>(null) }

    // Logger listener — posts all state updates back to Main so Compose sees them
    DisposableEffect(tool) {
        val listener: (String) -> Unit = { msg ->
            scope.launch(Dispatchers.Main) {
                when {
                    msg.startsWith("RESIZE_INFO: ")    -> { resizeInfo = msg.substringAfter("RESIZE_INFO: "); isRunning = false }
                    msg.startsWith("OUTPUT_PATH: ")    -> outputFilePath = msg.substringAfter("OUTPUT_PATH: ")
                    msg.startsWith("SUGGESTED_NAME: ") -> suggestedSaveName = msg.substringAfter("SUGGESTED_NAME: ")
                    msg.startsWith("Error: ")          -> { isRunning = false; toast = ToastData(msg.substringAfter("Error: "), isSuccess = false) }
                }
                logs += msg + "\n"
            }
        }
        Logger.onLog = listener
        onDispose { if (Logger.onLog == listener) Logger.onLog = null }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── header ─────────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "← Back",
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand).clickable { onBack() }.padding(end = 20.dp),
                    color = Color(0xFF4A90E2),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = tool.displayName,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { showLogs = !showLogs }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Toggle logs",
                        tint = if (showLogs) Color(0xFF4A90E2) else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (showLogs) {
                LogView(logs = logs, modifier = Modifier.weight(1f))
            } else {
                FileDropZone(
                    selectedFilePath = selectedFilePath,
                    selectedFileSize = selectedFileSize,
                    placeholder = "Tap to select a PDF",
                    onSelect = {
                        scope.launch {
                            kotlinx.coroutines.yield() // Ensure UI updates (ripple) before modal/native activity
                            pickFile(listOf("pdf"))?.let {
                                selectedFilePath = it
                                selectedFileSize = fileSystem.metadataOrNull(it.toPath())?.size
                                resizeInfo = null
                                logs = ""
                            }
                        }
                    },
                    onClear = {
                        selectedFilePath = null; selectedFileSize = null
                        resizeInfo = null; outputFilePath = null; suggestedSaveName = null
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (selectedFilePath != null && resizeInfo == null && !isRunning) {
                    ProfileSelector(selected = selectedProfile, onSelect = { selectedProfile = it })
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (isRunning) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CompressionProgress()
                }

                resizeInfo?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    CompressionResultCard(info = it)
                }

                Spacer(modifier = Modifier.weight(1f))

                // ── action buttons ─────────────────────────────────────────────
                if (resizeInfo == null) {
                    CompressPdfSubmitButton(
                        enabled = selectedFilePath != null && !isRunning,
                        onClick = {
                            selectedFilePath?.let { path ->
                                isRunning = true
                                outputFilePath = null
                                suggestedSaveName = null
                                scope.launch {
                                    withContext(Dispatchers.Default) {
                                        tool.run(listOf(path, "--profile", selectedProfile.toArg()))
                                    }
                                }
                            }
                        }
                    )
                } else {
                    if (outputFilePath != null) {
                        SaveButton(onClick = {
                            scope.launch {
                                kotlinx.coroutines.yield()
                                val result = saveCompressedFile(outputFilePath!!, suggestedSaveName)
                                withContext(Dispatchers.Main) {
                                    result.savedPath?.let { outputFilePath = it }
                                    toast = result.toast
                                }
                            }
                        })
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    CompressAnotherPdfFileButton(onClick = {
                        resizeInfo = null; selectedFilePath = null; selectedFileSize = null
                        outputFilePath = null; suggestedSaveName = null; logs = ""
                    })
                }
            }
        } // Column

        ToastHost(toast = toast, onDismiss = { toast = null })

    } // Box
}


