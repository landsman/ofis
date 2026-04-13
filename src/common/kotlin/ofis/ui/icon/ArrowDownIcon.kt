package ofis.ui.icon

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ArrowDownIcon(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val offsetY by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
    )

    Icon(
        imageVector = Icons.Default.KeyboardArrowDown,
        contentDescription = null,
        tint = Color(0xFF000000),
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .wrapContentWidth(Alignment.CenterHorizontally)
                .offset(y = offsetY.dp),
    )
}
