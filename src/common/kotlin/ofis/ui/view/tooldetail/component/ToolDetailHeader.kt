package ofis.ui.view.tooldetail.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

@Composable
fun ToolDetailHeader(
    tool: Tool,
    onBack: () -> Unit,
    actions: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
    ) {
        // Left — back button
        Text(
            text = "← Back",
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable { onBack() },
            color = Color(0xFF4A90E2),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )

        // Centre — tool title
        Text(
            text = tool.displayName,
            modifier = Modifier.align(Alignment.Center),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1A1A1A),
        )

        // Right — optional actions
        if (actions != null) {
            Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                actions()
            }
        }
    }
}
