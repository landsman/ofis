package ofis

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ofis.components.ui.DotGridBackground
import ofis.components.ToolSelectionScreen
import ofis.tool.Tool

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
                    currentTool!!.Screen(onBack = { currentTool = null })
                }
            }
        }
    }
}
