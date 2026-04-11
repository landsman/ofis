package ofis.ui.view.toolselection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ofis.i18n.LocalAppStrings
import ofis.tool.Tool
import ofis.tool.ToolRegistry
import ofis.ui.component.AppHeader
import ofis.ui.component.ScreenLayout
import ofis.ui.view.toolselection.component.ToolCard

@Composable
fun ToolSelectionView(
    onToolSelect: (Tool) -> Unit,
    onSettingsClick: (() -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
) {
    val strings = LocalAppStrings.current
    ScreenLayout(
        header = {
            AppHeader(
                title = "Ofis",
                subtitle = strings.appSubtitle,
                actions =
                    onSettingsClick?.let {
                        {
                            IconButton(onClick = it) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = strings.settings,
                                    tint = Color(0xFF888888),
                                )
                            }
                        }
                    },
            )
        },
        footer = footer,
    ) {
        val tools = ToolRegistry.list()
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 40.dp, end = 40.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(tools) { tool ->
                ToolCard(tool = tool, onClick = { onToolSelect(tool) })
            }
        }
    }
}
