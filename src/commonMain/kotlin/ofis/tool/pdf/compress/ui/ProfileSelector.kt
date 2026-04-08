package ofis.tool.pdf.compress.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ofis.ui.system.Badge
import ofis.tool.pdf.compress.model.CompressionProfile

@Composable
fun ProfileSelector(
    selected: CompressionProfile,
    onSelect: (CompressionProfile) -> Unit,
) {
    Text(
        text = "Compression Level",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = Color(0xFF1A1A1A)
    )
    Spacer(modifier = Modifier.height(12.dp))
    CompressionProfile.entries.forEach { profile ->
        ProfileOption(profile = profile, selected = profile == selected, onClick = { onSelect(profile) })
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun ProfileOption(
    profile: CompressionProfile,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) Color(0xFF4A4AFF) else Color(0xFFE0E0E0)
    val bgColor = if (selected) Color(0xFFF0F0FF) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(10.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF4A4AFF),
                unselectedColor = Color(0xFFCCCCCC)
            )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(profile.label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                if (profile == CompressionProfile.BALANCED) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(text = "Recommended")
                }
            }
            Text(text = profile.description, color = Color.Gray, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}
