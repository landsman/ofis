package ofis.ui.view.filesuggestion

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ofis.config.GlobalConfig
import ofis.i18n.LocalAppStrings
import ofis.platform.view.openUrl
import ofis.ui.component.AppHeader
import ofis.ui.component.ScreenLayout
import ofis.ui.system.AppButton

@Composable
fun UnsupportedFileView(
    filePath: String,
    onDismiss: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val fileName = filePath.substringAfterLast("/")
    val ext = filePath.substringAfterLast('.', "").lowercase()
    var note by remember { mutableStateOf("") }

    ScreenLayout(
        header = {
            AppHeader(
                title = fileName,
                subtitle = strings.dropSuggestNoTools(ext),
                onBack = onDismiss,
            )
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 40.dp, vertical = 8.dp),
        ) {
            Text(
                text = strings.dropSuggestRequestSupport,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = strings.dropSuggestRequestSupportNote,
                fontSize = 13.sp,
                color = Color(0xFF888888),
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = {
                    Text(strings.dropSuggestRequestSupportPlaceholder, color = Color(0xFFBBBBBB))
                },
                maxLines = 6,
            )
            Spacer(modifier = Modifier.height(20.dp))
            AppButton(
                onClick = {
                    val subject = strings.dropSuggestRequestSupportEmailSubject(ext)
                    val body = strings.dropSuggestRequestSupportEmailBody(ext, note)
                    val mailto = buildMailto(GlobalConfig.SUPPORT_EMAIL, subject, body)
                    openUrl(mailto)
                },
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text(strings.dropSuggestRequestSupportSend)
            }
        }
    }
}

private fun buildMailto(
    to: String,
    subject: String,
    body: String,
): String {
    fun encode(s: String) =
        s
            .replace("%", "%25")
            .replace(" ", "%20")
            .replace("\n", "%0A")
            .replace("&", "%26")
            .replace("?", "%3F")
            .replace("#", "%23")
    return "mailto:$to?subject=${encode(subject)}&body=${encode(body)}"
}
