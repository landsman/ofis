package ofis.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val DEFAULT_MAX_WIDTH = 860.dp

@Composable
fun ScreenLayout(
    header: @Composable () -> Unit,
    footer: (@Composable () -> Unit)? = null,
    maxWidth: Dp = DEFAULT_MAX_WIDTH,
    content: @Composable () -> Unit,
) {
    // Full-width outer shell — fills the window
    Column(modifier = Modifier.fillMaxSize()) {
        // Constrained + centred column for header / content / footer
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .widthIn(max = maxWidth)
                    .fillMaxWidth(),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) { header() }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) { content() }
            if (footer != null) {
                Box(modifier = Modifier.fillMaxWidth()) { footer() }
            }
        }
    }
}
