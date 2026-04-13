package ofis.ui.system

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ofis.i18n.LocalAppStrings

@Composable
fun FileDropZone(
    selectedFilePath: String?,
    selectedFileSize: Long?,
    placeholder: String,
    onSelect: () -> Unit,
    onClear: () -> Unit,
) {
    val strings = LocalAppStrings.current

    if (selectedFilePath != null) {
        SelectedFileCard(
            filePath = selectedFilePath,
            fileSize = selectedFileSize,
            onClear = onClear,
        )
    } else {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                    .handClickable(onClick = onSelect)
                    .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "📄", fontSize = 40.sp, modifier = Modifier.padding(bottom = 8.dp))
                Text(placeholder, color = Color(0xFF555555), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(strings.orDragAndDrop, color = Color(0xFFAAAAAA), fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun DropOverlay() {
    val strings = LocalAppStrings.current
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xEEF5FBFF))
                .border(3.dp, Color(0xFF4A90E2), RoundedCornerShape(12.dp))
                .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "📄", fontSize = 72.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = strings.pdfCompressDropHere,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2A70C2),
            )
        }
    }
}
