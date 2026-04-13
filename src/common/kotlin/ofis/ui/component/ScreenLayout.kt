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
    // Measure actual window width to drive responsive breakpoints,
    // then render the header at full width (white bg spans the window)
    // while content is capped at maxWidth.
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val windowWidth = this.maxWidth.toWindowWidth()
        CompositionLocalProvider(LocalWindowWidth provides windowWidth) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header — full window width, no max-width cap
                Box(modifier = Modifier.fillMaxWidth()) { header() }

                // Content + footer — centred and capped at maxWidth
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                            .widthIn(max = maxWidth)
                            .fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 24.dp)) { content() }
                        if (footer != null) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 40.dp),
                            ) { footer() }
                        }
                    }
                }
            }
        }
    }
}
