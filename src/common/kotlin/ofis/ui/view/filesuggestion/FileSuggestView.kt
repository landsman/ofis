package ofis.ui.view.filesuggestion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ofis.i18n.LocalAppStrings
import ofis.tool.Tool
import ofis.ui.component.AppHeader
import ofis.ui.component.ScreenLayout
import ofis.ui.icon.ArrowDownIcon
import ofis.ui.system.SelectedFileCard
import ofis.ui.view.toolselection.component.ToolCard

@Composable
fun FileSuggestView(
    filePath: String,
    fileSize: Long? = null,
    matchingTools: List<Tool>,
    onToolSelect: (Tool) -> Unit,
    onDismiss: () -> Unit,
) {
    if (matchingTools.isEmpty()) {
        UnsupportedFileView(filePath = filePath, fileSize = fileSize, onDismiss = onDismiss)
        return
    }

    val strings = LocalAppStrings.current

    ScreenLayout(
        header = {
            AppHeader(
                title = strings.dropSuggestSubtitle,
                onBack = onDismiss,
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 40.dp, end = 40.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SelectedFileCard(filePath = filePath, fileSize = fileSize)
            }
            item {
                ArrowDownIcon()
            }
            items(matchingTools) { tool ->
                ToolCard(tool = tool, onClick = { onToolSelect(tool) }, buttonLabel = strings.select)
            }
        }
    }
}
