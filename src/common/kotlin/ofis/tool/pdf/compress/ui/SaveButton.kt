package ofis.tool.pdf.compress.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ofis.i18n.LocalAppStrings
import ofis.ui.system.AppButton

@Composable
fun SaveButton(onClick: () -> Unit) {
    AppButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4A4AFF),
                contentColor = Color.White,
            ),
    ) {
        Text(LocalAppStrings.current.pdfCompress.save, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
