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
import ofis.generated.resources.Res
import ofis.generated.resources.compress_another_file
import ofis.ui.system.AppButton
import org.jetbrains.compose.resources.stringResource

/**
 * Button for compressing another PDF file.
 */
@Composable
fun CompressAnotherPdfFileButton(onClick: () -> Unit) {
    AppButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF0F0F0),
                contentColor = Color(0xFF333333),
            ),
    ) {
        Text(stringResource(Res.string.compress_another_file), fontWeight = FontWeight.Medium, fontSize = 15.sp)
    }
}
