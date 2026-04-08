package ofis.components.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private const val TOAST_DURATION_MS = 3_500L

data class ToastData(val message: String, val isSuccess: Boolean)

@Composable
fun ToastHost(toast: ToastData?, onDismiss: () -> Unit) {
    LaunchedEffect(toast) {
        if (toast != null) {
            delay(TOAST_DURATION_MS)
            onDismiss()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(
            visible = toast != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        ) {
            if (toast != null) {
                ToastCard(toast)
            }
        }
    }
}

@Composable
private fun ToastCard(toast: ToastData) {
    val bgColor = if (toast.isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828)
    val icon = if (toast.isSuccess) "✓" else "✕"

    Row(
        modifier = Modifier
            .padding(top = 24.dp, start = 16.dp, end = 16.dp)
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(icon, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(toast.message, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
