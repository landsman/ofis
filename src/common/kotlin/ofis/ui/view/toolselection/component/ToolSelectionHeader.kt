package ofis.ui.view.toolselection.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ofis.i18n.LocalAppStrings
import ofis.ui.system.LocalWindowWidth
import ofis.ui.system.WindowWidth

@Composable
fun ToolSelectionHeader(onSettingsClick: (() -> Unit)? = null) {
    val strings = LocalAppStrings.current
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

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Ofis",
                fontSize = titleSize,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF000000),
            )
            Text(
                text = strings.appSubtitle,
                fontSize = subtitleSize,
                color = Color(0xFF666666),
            )
            Spacer(modifier = Modifier.height(bottomSpacing))
        }

        if (onSettingsClick != null) {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = strings.settings,
                    tint = Color(0xFF888888),
                )
            }
        }
    }
}
