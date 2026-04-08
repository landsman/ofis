package ofis.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ofis.tool.Tool
import ofis.ToolRegistry

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
            .pointerHoverIcon(PointerIcon.Hand, overrideDescendants = false)
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = tool.displayName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = tool.description, color = Color.Gray, fontSize = 14.sp)
        }
        
        AppButton(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF0F0F0))
        ) {
            Text("Open")
        }
    }
}

