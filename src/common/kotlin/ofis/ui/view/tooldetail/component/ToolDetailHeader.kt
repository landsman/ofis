package ofis.ui.view.tooldetail.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ofis.i18n.LocalAppStrings
import ofis.tool.Tool
import ofis.ui.system.LocalWindowWidth
import ofis.ui.system.WindowWidth
import ofis.ui.system.handClickable

@Composable
fun ToolDetailHeader(
    tool: Tool? = null,
    titleOverride: String? = null,
    onBack: () -> Unit,
    actions: (@Composable () -> Unit)? = null,
) {
    val strings = LocalAppStrings.current
    val window = LocalWindowWidth.current
    val titleSize =
        when (window) {
            WindowWidth.Compact -> 18.sp
            WindowWidth.Medium -> 22.sp
            WindowWidth.Expanded -> 28.sp
        }
    val backSize =
        when (window) {
            WindowWidth.Compact -> 13.sp
            WindowWidth.Medium -> 14.sp
            WindowWidth.Expanded -> 16.sp
        }

    Box(
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
    ) {
        // Left — back button
        Text(
            text = strings.common.back,
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .handClickable { onBack() },
            color = Color(0xFF4A90E2),
            fontSize = backSize,
            fontWeight = FontWeight.Bold,
        )

        // Centre — tool title
        val title = titleOverride ?: tool?.localizedDisplayName() ?: ""
        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center),
            fontSize = titleSize,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1A1A1A),
        )

        // Right — optional actions (icons, badges, etc.)
        if (actions != null) {
            Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                actions()
            }
        }
    }
}
