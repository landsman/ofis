package ofis.ui.component

import androidx.compose.runtime.Composable

@Composable
fun DetailView(
    title: String,
    onBack: () -> Unit,
    headerActions: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    ScreenLayout(
        header = {
            AppHeader(
                title = title,
                onBack = onBack,
                actions = headerActions,
            )
        },
        footer = footer,
    ) {
        content()
    }
}
