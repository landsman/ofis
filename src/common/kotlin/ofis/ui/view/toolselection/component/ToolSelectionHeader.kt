package ofis.ui.view.toolselection.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ToolSelectionHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Ofis",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF000000),
        )
        Text(
            text = "Tooling Platform",
            fontSize = 18.sp,
            color = Color(0xFF666666),
        )
        Spacer(modifier = Modifier.height(40.dp))
    }
}
