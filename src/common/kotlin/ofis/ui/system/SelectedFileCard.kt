package ofis.ui.system

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
fun SelectedFileCard(
    filePath: String,
    fileSize: Long? = null,
    warningText: String? = null,
    onClear: (() -> Unit)? = null,
) {
    val strings = LocalAppStrings.current

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "📄", fontSize = 32.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = filePath.substringAfterLast("/"),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color(0xFF333333),
            )
            if (fileSize != null) {
                Text(text = formatSize(fileSize), color = Color.Gray, fontSize = 13.sp)
            }
            if (warningText != null) {
                Text(
                    text = warningText,
                    fontSize = 13.sp,
                    color = Color(0xFFCC3333),
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        if (onClear != null) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = strings.clearSelection,
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
