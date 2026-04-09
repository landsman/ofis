package ofis.ui.view.tooldetail

import androidx.compose.runtime.Composable
import ofis.tool.Tool
import ofis.ui.component.ScreenLayout
import ofis.ui.view.tooldetail.component.ToolDetailHeader

@Composable
fun ToolDetailView(
    tool: Tool,
    onBack: () -> Unit,
    headerActions: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    ScreenLayout(
        header = { ToolDetailHeader(tool = tool, onBack = onBack, actions = headerActions) },
        footer = footer,
    ) {
        content()
    }
}
