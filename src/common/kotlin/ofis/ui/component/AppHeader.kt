package ofis.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ofis.i18n.LocalAppStrings
import ofis.ui.system.LocalWindowWidth
import ofis.ui.system.WindowWidth

/**
 * Unified app header used on every screen.
 *
 * - [onBack] == null  → home style: large left-aligned [title], optional [subtitle], [actions] top-right
 * - [onBack] != null  → detail style: back button left, centred [title], [actions] right
 */
@Composable
fun AppHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
) {
    val strings = LocalAppStrings.current
    val window = LocalWindowWidth.current
    val isDetail = onBack != null

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(start = 40.dp, end = 40.dp, top = 40.dp, bottom = 24.dp),
    ) {
        if (isDetail) {
            val titleSize =
                when (window) {
                    WindowWidth.Compact -> 14.sp
                    WindowWidth.Medium -> 16.sp
                    WindowWidth.Expanded -> 20.sp
                }
            val backSize =
                when (window) {
                    WindowWidth.Compact -> 13.sp
                    WindowWidth.Medium -> 14.sp
                    WindowWidth.Expanded -> 16.sp
                }

            BackButton(
                text = strings.back,
                fontSize = backSize,
                onClick = onBack!!,
                modifier = Modifier.align(Alignment.CenterStart),
            )
            Text(
                text = title,
                modifier = Modifier.align(Alignment.Center),
                fontSize = titleSize,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1A1A),
            )
            if (actions != null) {
                Row(modifier = Modifier.align(Alignment.CenterEnd)) { actions() }
            }
        } else {
            val titleSize =
                when (window) {
                    WindowWidth.Compact -> 28.sp
                    WindowWidth.Medium -> 36.sp
                    WindowWidth.Expanded -> 42.sp
                }
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF000000),
                )
                if (subtitle != null) {
                    val subtitleSize =
                        when (window) {
                            WindowWidth.Compact -> 14.sp
                            WindowWidth.Medium -> 16.sp
                            WindowWidth.Expanded -> 18.sp
                        }
                    Text(
                        text = subtitle,
                        fontSize = subtitleSize,
                        color = Color(0xFF666666),
                    )
                }
            }
            if (actions != null) {
                Row(modifier = Modifier.align(Alignment.TopEnd)) { actions() }
            }
        }
    }
}

@Composable
private fun BackButton(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bgColor by animateColorAsState(
        targetValue = if (isHovered) Color(0xFFEEF4FF) else Color.Transparent,
        animationSpec = tween(150),
    )

    Text(
        text = text,
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor)
                .hoverable(interactionSource)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .pointerHoverIcon(PointerIcon.Hand)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        color = Color(0xFF4A90E2),
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
    )
}
