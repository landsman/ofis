package ofis.ui.system

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DotGridBackground() {
    Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA))) {
        val spacing = 20.dp.toPx()
        val dotSize = 2.dp.toPx()
        val dotColor = Color(0xFFD9D9D9)

        for (x in 0..(size.width / spacing).toInt()) {
            for (y in 0..(size.height / spacing).toInt()) {
                drawCircle(
                    color = dotColor,
                    radius = dotSize / 2,
                    center = Offset(x * spacing, y * spacing),
                )
            }
        }
    }
}
