package ofis.ui.view.tool_selection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ofis.tool.Tool
import ofis.tool.ToolRegistry
import ofis.ui.view.tool_selection.component.ToolCard

@Composable
fun ToolSelectionView(onToolSelect: (Tool) -> Unit) {
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

