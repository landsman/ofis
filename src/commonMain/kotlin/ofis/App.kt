package ofis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun App() {
    var currentTool by remember { mutableStateOf<Tool?>(null) }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            DotGridBackground()
            
            Column(modifier = Modifier.fillMaxSize().padding(40.dp)) {
                if (currentTool == null) {
                    ToolSelectionScreen(onToolSelect = { currentTool = it })
                } else {
                    ToolDetailScreen(tool = currentTool!!, onBack = { currentTool = null })
                }
            }
        }
    }
}

@Composable
fun DotGridBackground() {
    Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA))) {
        val spacing = 20.dp.toPx()
        val dotSize = 2.dp.toPx()
        val dotColor = Color(0xFFD9D9D9)
        
        for (x in 0..(size.width / spacing).toInt()) {
            for (y in 0..(size.height / spacing).toInt()) {
                drawCircle(
                    color = dotColor,
                    radius = dotSize / 2,
                    center = Offset(x * spacing, y * spacing)
                )
            }
        }
    }
}

@Composable
fun ToolSelectionScreen(onToolSelect: (Tool) -> Unit) {
    Column {
        Text(
            text = "Ofis",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF000000)
        )
        Text(
            text = "Tooling Platform",
            fontSize = 18.sp,
            color = Color(0xFF666666)
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        val tools = ToolRegistry.list()
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(tools) { tool ->
                ToolCard(tool = tool, onClick = { onToolSelect(tool) })
            }
        }
    }
}

@Composable
fun ToolCard(tool: Tool, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = tool.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = tool.description, color = Color.Gray, fontSize = 14.sp)
        }
        
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF0F0F0))
        ) {
            Text("Open")
        }
    }
}

@Composable
fun ToolDetailScreen(tool: Tool, onBack: () -> Unit) {
    var logs by remember { mutableStateOf("") }
    var selectedFilePath by remember { mutableStateOf<String?>(null) }
    
    // Wire up Logger to our UI
    LaunchedEffect(tool) {
        Logger.onLog = { msg ->
            logs += msg + "\n"
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
            Text(text = if (logs.isEmpty()) "No logs yet..." else logs)
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
