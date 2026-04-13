package ofis.ui.view.toolselection.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import ofis.tool.Tool
import ofis.ui.system.AppButton

private val shape = RoundedCornerShape(12.dp)

@Composable
fun ToolCard(
    tool: Tool,
    onClick: () -> Unit,
    buttonLabel: String? = null,
) {
    val strings = LocalAppStrings.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bgColor by animateColorAsState(
        targetValue = if (isHovered) Color(0xFFF4F4FF) else Color.White,
        animationSpec = tween(150),
    )
    val borderColor by animateColorAsState(
        targetValue = if (isHovered) Color(0xFFAAAAAA) else Color(0xFFE0E0E0),
        animationSpec = tween(150),
    )

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(shape)
                .background(bgColor, shape)
                .border(1.dp, borderColor, shape)
                .hoverable(interactionSource)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .pointerHoverIcon(PointerIcon.Hand)
                .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = tool.localizedDisplayName(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = tool.localizedDescription(), color = Color.Gray, fontSize = 14.sp)
        }

        AppButton(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(buttonLabel ?: strings.open)
        }
    }
}
