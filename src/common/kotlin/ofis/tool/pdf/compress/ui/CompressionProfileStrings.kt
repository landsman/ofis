package ofis.tool.pdf.compress.ui

import androidx.compose.runtime.Composable
import ofis.i18n.LocalAppStrings
import ofis.tool.pdf.compress.model.CompressionProfile

@Composable
fun CompressionProfile.localizedLabel(): String {
    val strings = LocalAppStrings.current
    return when (this) {
        CompressionProfile.HIGH_QUALITY -> strings.pdfCompressProfileHighQuality
        CompressionProfile.BALANCED -> strings.pdfCompressProfileBalanced
        CompressionProfile.MAXIMUM -> strings.pdfCompressProfileMaximum
    }
}

@Composable
fun CompressionProfile.localizedDescription(): String {
    val strings = LocalAppStrings.current
    return when (this) {
        CompressionProfile.HIGH_QUALITY -> strings.pdfCompressProfileHighQualityDesc
        CompressionProfile.BALANCED -> strings.pdfCompressProfileBalancedDesc
        CompressionProfile.MAXIMUM -> strings.pdfCompressProfileMaximumDesc
    }
}
