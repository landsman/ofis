package ofis.components.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private const val SLOW_THRESHOLD_MS = 3_000L

@Composable
fun CompressionProgress() {
    var isSlow by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(SLOW_THRESHOLD_MS)
        isSlow = true
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F4FF), RoundedCornerShape(12.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isSlow) {
                CircularProgressIndicator(color = Color(0xFF4A4AFF), strokeWidth = 3.dp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Almost there!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Compressing your PDF...", color = Color(0xFF4A4AFF), fontSize = 14.sp)
            } else {
                Text("Making your PDF file smaller...", fontSize = 15.sp, color = Color(0xFF444444))
            }
        }
    }
}
