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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ofis.config.GlobalConfig
import ofis.i18n.LocalAppStrings
import ofis.platform.view.openUrl
import ofis.ui.component.AppHeader
import ofis.ui.component.ScreenLayout
import ofis.ui.system.AppButton
import ofis.ui.system.SelectedFileCard

@Composable
fun UnsupportedFileView(
    filePath: String,
    fileSize: Long? = null,
    onDismiss: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val ext = filePath.substringAfterLast('.', "").lowercase()
    var note by remember { mutableStateOf(TextFieldValue("")) }

    ScreenLayout(
        header = {
            AppHeader(
                title = strings.fileSelected,
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
            SelectedFileCard(
                filePath = filePath,
                fileSize = fileSize,
                warningText = strings.dropSuggestNoTools(ext),
            )
            Spacer(modifier = Modifier.height(20.dp))
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
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .onPreviewKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown && event.isMetaPressed && event.key == Key.A) {
                                note = note.copy(selection = TextRange(0, note.text.length))
                                true
                            } else {
                                false
                            }
                        },
                placeholder = {
                    Text(strings.dropSuggestRequestSupportPlaceholder, color = Color(0xFFBBBBBB))
                },
                maxLines = 6,
            )
            Spacer(modifier = Modifier.height(20.dp))
            AppButton(
                onClick = {
                    val subject = strings.dropSuggestRequestSupportEmailSubject(ext)
                    val body = strings.dropSuggestRequestSupportEmailBody(ext, note.text)
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
): String = "mailto:$to?subject=${encodeMailtoField(subject)}&body=${encodeMailtoField(body)}"

/**
 * Percent-encodes a mailto header value per RFC 6068.
 * Encodes every byte of non-ASCII and reserved characters via UTF-8.
 * Unreserved characters (RFC 3986) are passed through unchanged.
 */
private fun encodeMailtoField(value: String): String {
    val unreserved = { c: Char -> c.isLetterOrDigit() || c in "-._~" }
    return buildString {
        for (byte in value.encodeToByteArray()) {
            val c = byte.toInt().and(0xFF)
            val ch = c.toChar()
            if (unreserved(ch)) append(ch) else append('%').append(c.toString(16).uppercase().padStart(2, '0'))
        }
    }
}
