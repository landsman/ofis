package ofis.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ofis.components.detail.*
import ofis.config.Logger
import ofis.platform.availableDiskSpace
import ofis.platform.fileSystem
import ofis.platform.pickFile
import ofis.platform.saveFile
import ofis.tool.Tool
import ofis.tool.pdf.compress.CompressionProfile
import ofis.utils.format.formatSize
import okio.Path.Companion.toPath

@Composable
fun ToolDetailScreen(tool: Tool, onBack: () -> Unit) {
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
                    msg.startsWith("Error: ")          -> isRunning = false
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
                modifier = Modifier.clickable { onBack() }.padding(end = 16.dp),
                color = Color(0xFF4A90E2),
                fontWeight = FontWeight.Bold
            )
            Text(text = tool.displayName, fontSize = 24.sp, fontWeight = FontWeight.Bold)
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
                onSelect = {
                    pickFile(listOf("pdf"))?.let {
                        selectedFilePath = it
                        selectedFileSize = fileSystem.metadataOrNull(it.toPath())?.size
                        resizeInfo = null
                        logs = ""
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
                Button(
                    onClick = {
                        selectedFilePath?.let { path ->
                            isRunning = true
                            outputFilePath = null
                            suggestedSaveName = null
                            val profileArg = when (selectedProfile) {
                                CompressionProfile.HIGH_QUALITY -> "high"
                                CompressionProfile.BALANCED     -> "balanced"
                                CompressionProfile.MAXIMUM      -> "max"
                            }
                            // Run compression on background thread — keeps UI responsive
                            scope.launch {
                                withContext(Dispatchers.Default) {
                                    tool.run(listOf(path, "--profile", profileArg))
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = selectedFilePath != null && !isRunning,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF4A4AFF),
                        contentColor = Color.White,
                        disabledBackgroundColor = Color(0xFFBBBBBB)
                    )
                ) {
                    Text("Compress PDF", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } else {
                // Save — only shown when there is an actual smaller output file
                if (outputFilePath != null) {
                    Button(
                        onClick = {
                            val dest = saveFile(suggestedSaveName ?: "compressed.pdf")
                            if (dest != null && outputFilePath != null) {
                                scope.launch {
                                    withContext(Dispatchers.Default) {
                                        val srcPath = outputFilePath!!.toPath()
                                        val destPath = dest.toPath()
                                        val destDir = destPath.parent?.toString() ?: "/"

                                        // Proactive disk space check
                                        val fileSize = fileSystem.metadataOrNull(srcPath)?.size ?: 0L
                                        val freeSpace = availableDiskSpace(destDir)
                                        if (freeSpace < fileSize) {
                                            withContext(Dispatchers.Main) {
                                                toast = ToastData(
                                                    "Not enough disk space. Need ${formatSize(fileSize)}, only ${formatSize(freeSpace)} available.",
                                                    isSuccess = false
                                                )
                                            }
                                            return@withContext
                                        }

                                        try {
                                            try {
                                                fileSystem.atomicMove(srcPath, destPath)
                                            } catch (_: Exception) {
                                                // atomicMove fails across volumes — copy + delete
                                                fileSystem.copy(srcPath, destPath)
                                                fileSystem.delete(srcPath)
                                            }
                                            withContext(Dispatchers.Main) {
                                                outputFilePath = dest
                                                toast = ToastData("File saved successfully.", isSuccess = true)
                                            }
                                        } catch (e: Exception) {
                                            val reason = when {
                                                e.message?.contains("No space left", ignoreCase = true) == true ||
                                                e.message?.contains("ENOSPC", ignoreCase = true) == true ->
                                                    "Not enough disk space."
                                                e.message?.contains("Permission", ignoreCase = true) == true ->
                                                    "Permission denied."
                                                else -> e.message ?: "Unknown error."
                                            }
                                            withContext(Dispatchers.Main) {
                                                toast = ToastData("Failed to save: $reason", isSuccess = false)
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF4A4AFF),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Save compressed PDF", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Button(
                    onClick = {
                        resizeInfo = null; selectedFilePath = null; selectedFileSize = null
                        outputFilePath = null; suggestedSaveName = null; logs = ""
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFFF0F0F0),
                        contentColor = Color(0xFF333333)
                    )
                ) {
                    Text("Compress another file", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                }
            }
        }
    } // Column

    ToastHost(toast = toast, onDismiss = { toast = null })

    } // Box
}
