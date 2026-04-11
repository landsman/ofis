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

/**
 * Button for submitting PDF compression request, main CTA button.
 */
@Composable
fun CompressPdfSubmitButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    AppButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4A4AFF),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFBBBBBB),
            ),
        elevation =
            ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp,
            ),
    ) {
        Text(LocalAppStrings.current.pdfCompress.button, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}
