package ofis.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ofis.utils.format.formatSize

@Composable
fun FileDropZone(
    selectedFilePath: String?,
    selectedFileSize: Long?,
    onSelect: () -> Unit,
    onClear: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (selectedFilePath == null) 120.dp else 80.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .pointerHoverIcon(PointerIcon.Hand, overrideDescendants = false)
            .clickable(enabled = selectedFilePath == null, onClick = onSelect)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selectedFilePath == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "📄", fontSize = 40.sp, modifier = Modifier.padding(bottom = 8.dp))
                Text("Tap to select a PDF", color = Color(0xFF555555), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text("or drag and drop here", color = Color(0xFFAAAAAA), fontSize = 13.sp)
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📄", fontSize = 32.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedFilePath.split("/").last(),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color(0xFF333333)
                    )
                    if (selectedFileSize != null) {
                        Text(text = formatSize(selectedFileSize), color = Color.Gray, fontSize = 13.sp)
                    }
                }
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear selection",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(28.dp)
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable { onClear() }
                        .padding(4.dp)
                )
            }
        }
    }
}
