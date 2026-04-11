package ofis.ui.view.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ofis.i18n.AppLanguage
import ofis.i18n.LocalAppStrings
import ofis.ui.component.ScreenLayout
import ofis.ui.system.handClickable
import ofis.ui.view.tooldetail.component.ToolDetailHeader

@Composable
fun SettingsView(
    selectedLanguage: AppLanguage,
    onLanguageSelect: (AppLanguage) -> Unit,
    onBack: () -> Unit,
) {
    val strings = LocalAppStrings.current

    ScreenLayout(
        header = {
            ToolDetailHeader(
                titleOverride = strings.settingsTitle,
                onBack = onBack,
            )
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 24.dp),
        ) {
            Text(
                text = strings.languageLabel,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1A1A1A),
            )
            Spacer(modifier = Modifier.height(12.dp))

            listOf(
                AppLanguage.SYSTEM to strings.languageSystem,
                AppLanguage.ENGLISH to strings.languageEnglish,
                AppLanguage.CZECH to strings.languageCzech,
            ).forEach { (language, label) ->
                LanguageOption(
                    label = label,
                    selected = language == selectedLanguage,
                    onClick = { onLanguageSelect(language) },
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun LanguageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) Color(0xFF4A4AFF) else Color(0xFFE0E0E0)
    val bgColor = if (selected) Color(0xFFF0F0FF) else Color.White

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(bgColor, RoundedCornerShape(10.dp))
                .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
                .handClickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
            colors =
                RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF4A4AFF),
                    unselectedColor = Color(0xFFCCCCCC),
                ),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, fontWeight = FontWeight.Medium, fontSize = 16.sp)
    }
}
