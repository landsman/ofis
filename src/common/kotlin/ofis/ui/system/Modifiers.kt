package ofis.ui.system

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

fun Modifier.handClickable(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier =
    this
        .pointerHoverIcon(PointerIcon.Hand)
        .clickable(enabled = enabled, onClick = onClick)
