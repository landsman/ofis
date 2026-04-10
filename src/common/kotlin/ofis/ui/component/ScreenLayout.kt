package ofis.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ofis.ui.system.LocalWindowWidth
import ofis.ui.system.toWindowWidth

private val DEFAULT_MAX_WIDTH = 860.dp

@Composable
fun ScreenLayout(
    header: @Composable () -> Unit,
    footer: (@Composable () -> Unit)? = null,
    maxWidth: Dp = DEFAULT_MAX_WIDTH,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .widthIn(max = maxWidth)
                    .fillMaxWidth(),
        ) {
            val windowWidth = this.maxWidth.toWindowWidth()
            CompositionLocalProvider(LocalWindowWidth provides windowWidth) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(start = 40.dp, end = 40.dp, top = 40.dp)) { header() }
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) { content() }
                    if (footer != null) {
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 40.dp)) { footer() }
                    }
                }
            }
        }
    }
}
