package ofis.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ofis.config.Logger
import ofis.tool.Tool
import ofis.platform.pickFile

@Composable
fun ToolDetailScreen(tool: Tool, onBack: () -> Unit) {
    var logs by remember { mutableStateOf("") }
    var showLogs by remember { mutableStateOf(false) }
    var selectedFilePath by remember { mutableStateOf<String?>(null) }
    var resizeInfo by remember { mutableStateOf<String?>(null) }
    
    // Wire up Logger to our UI
    DisposableEffect(tool) {
        val listener: (String) -> Unit = { msg ->
            if (msg.startsWith("RESIZE_INFO: ")) {
                resizeInfo = msg.substringAfter("RESIZE_INFO: ")
            }
            logs += msg + "\n"
        }
        Logger.onLog = listener
        onDispose {
            if (Logger.onLog == listener) {
                Logger.onLog = null
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "← Back",
                modifier = Modifier.clickable { onBack() }.padding(end = 16.dp),
                color = Color.Blue,
                fontWeight = FontWeight.Bold
            )
            Text(text = tool.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(onClick = { showLogs = !showLogs }) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Show Logs",
                    tint = if (showLogs) Color.Blue else Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            if (showLogs) {
                LazyColumn {
                    item {
                        Text(
                            text = if (logs.isEmpty()) "No logs yet..." else logs,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (resizeInfo != null) {
                        Text(
                            text = "Compression Results",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = resizeInfo!!,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        val percentage = resizeInfo!!.substringAfter("(").substringBefore("%").toIntOrNull()
                        if (percentage != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress = percentage.toFloat() / 100f,
                                modifier = Modifier.fillMaxWidth(0.6f).height(8.dp),
                                color = Color(0xFF4CAF50),
                                backgroundColor = Color(0xFFE0E0E0)
                            )
                        }
                    } else {
                        Text(
                            text = "Select a file and run the tool to see results",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    val path = pickFile(listOf("pdf"))
                    if (path != null) {
                        selectedFilePath = path
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF0F0F0))
            ) {
                Text(if (selectedFilePath == null) "Select PDF File" else "Change File")
            }
            
            if (selectedFilePath != null) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedFilePath!!.split("/").last(),
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        
        Button(
            onClick = {
                if (selectedFilePath != null) {
                    Logger.info("Running ${tool.name} with file: $selectedFilePath")
                    tool.run(listOf(selectedFilePath!!))
                } else {
                    Logger.info("Please select a file first.")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedFilePath != null,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Run Tool")
        }
    }
}
