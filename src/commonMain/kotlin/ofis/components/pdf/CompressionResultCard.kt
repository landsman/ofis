package ofis.components.pdf

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Expects info in the format: "259.8 KB → 180.2 KB (31%)" */
@Composable
fun CompressionResultCard(info: String) {
    val parts = info.split(" → ")
    val original = parts.getOrNull(0) ?: ""
    val rest = parts.getOrNull(1) ?: ""
    val compressed = rest.substringBefore(" (")
    val savedPercent = rest.substringAfter("(").substringBefore("%").toIntOrNull()
    val alreadyOptimal = savedPercent != null && savedPercent <= 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📊", fontSize = 32.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Compression Complete", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        if (alreadyOptimal) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF8E1), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "This PDF is already well-optimized. The compressed version isn't smaller.",
                    color = Color(0xFF795548),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SizeLabel(value = original, caption = "Original", color = Color(0xFF555555))
            Text("→", fontSize = 20.sp, color = Color.Gray)
            SizeLabel(
                value = compressed,
                caption = "Compressed",
                color = if (alreadyOptimal) Color(0xFFFF5722) else Color(0xFF4CAF50)
            )
        }

        if (savedPercent != null && savedPercent > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = savedPercent / 100f,
                modifier = Modifier.fillMaxWidth(0.8f).height(6.dp),
                color = Color(0xFF4CAF50),
                backgroundColor = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$savedPercent% smaller",
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun SizeLabel(value: String, caption: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
        Text(caption, color = Color.Gray, fontSize = 12.sp)
    }
}
