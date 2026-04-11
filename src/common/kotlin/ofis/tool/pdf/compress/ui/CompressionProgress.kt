package ofis.tool.pdf.compress.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ofis.i18n.LocalAppStrings
import kotlin.time.Duration.Companion.milliseconds

private const val SLOW_THRESHOLD_MS = 3_000L

/**
 * Progress indicator for the compression process.
 */
@Composable
fun CompressionProgress() {
    var isSlow by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    LaunchedEffect(Unit) {
        delay(SLOW_THRESHOLD_MS.milliseconds)
        isSlow = true
    }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F4FF), RoundedCornerShape(12.dp))
                .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFF4A4AFF), strokeWidth = 3.dp)
            Spacer(modifier = Modifier.height(12.dp))
            if (isSlow) {
                Text(strings.almostThere, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(strings.compressingPdf, color = Color(0xFF4A4AFF), fontSize = 14.sp)
            } else {
                Text(strings.makingPdfSmaller, fontSize = 15.sp, color = Color(0xFF444444))
            }
        }
    }
}
