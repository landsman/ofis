package ofis.ui.view.toolselection.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ofis.ui.system.LocalWindowWidth
import ofis.ui.system.WindowWidth

@Composable
fun ToolSelectionHeader() {
    val window = LocalWindowWidth.current
    val titleSize =
        when (window) {
            WindowWidth.Compact -> 28.sp
            WindowWidth.Medium -> 36.sp
            WindowWidth.Expanded -> 42.sp
        }
    val subtitleSize =
        when (window) {
            WindowWidth.Compact -> 14.sp
            WindowWidth.Medium -> 16.sp
            WindowWidth.Expanded -> 18.sp
        }
    val bottomSpacing =
        when (window) {
            WindowWidth.Compact -> 24.dp
            WindowWidth.Medium -> 32.dp
            WindowWidth.Expanded -> 40.dp
        }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Ofis",
            fontSize = titleSize,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF000000),
        )
        Text(
            text = "Tooling Platform",
            fontSize = subtitleSize,
            color = Color(0xFF666666),
        )
        Spacer(modifier = Modifier.height(bottomSpacing))
    }
}
