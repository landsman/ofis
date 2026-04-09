package ofis

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ofis.config.Logger
import ofis.tool.Tool
import ofis.ui.system.DotGridBackground
import ofis.ui.view.toolselection.ToolSelectionView

@Composable
fun App() {
    var currentTool by remember { mutableStateOf<Tool?>(null) }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            DotGridBackground()

            Column(modifier = Modifier.fillMaxSize().padding(40.dp)) {
                if (currentTool == null) {
                    ToolSelectionView(onToolSelect = {
                        Logger.debug("navigate → ${it.name}")
                        currentTool = it
                    })
                } else {
                    currentTool!!.Screen(onBack = {
                        Logger.debug("navigate → home")
                        currentTool = null
                    })
                }
            }
        }
    }
}
