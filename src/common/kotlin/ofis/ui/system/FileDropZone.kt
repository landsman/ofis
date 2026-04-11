package ofis.ui.system

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ofis.i18n.LocalAppStrings
import ofis.utils.format.formatSize

@Composable
fun FileDropZone(
    selectedFilePath: String?,
    selectedFileSize: Long?,
    placeholder: String,
    onSelect: () -> Unit,
    onClear: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val borderColor = Color(0xFFE0E0E0)

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(if (selectedFilePath == null) 160.dp else 100.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .handClickable(enabled = selectedFilePath == null, onClick = onSelect)
                .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (selectedFilePath == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "📄", fontSize = 40.sp, modifier = Modifier.padding(bottom = 8.dp))
                Text(placeholder, color = Color(0xFF555555), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(strings.common.orDragAndDrop, color = Color(0xFFAAAAAA), fontSize = 13.sp)
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
                        color = Color(0xFF333333),
                    )
                    if (selectedFileSize != null) {
                        Text(text = formatSize(selectedFileSize), color = Color.Gray, fontSize = 13.sp)
                    }
                }
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = strings.common.clearSelection,
                    tint = Color.Gray,
                    modifier =
                        Modifier
                            .size(28.dp)
                            .handClickable { onClear() }
                            .padding(4.dp),
                )
            }
        }
    }
}
