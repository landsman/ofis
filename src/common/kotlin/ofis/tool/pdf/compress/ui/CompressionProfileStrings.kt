package ofis.tool.pdf.compress.ui

import androidx.compose.runtime.Composable
import ofis.i18n.LocalAppStrings
import ofis.tool.pdf.compress.model.CompressionProfile

@Composable
fun CompressionProfile.localizedLabel(): String {
    val strings = LocalAppStrings.current
    val p = strings.pdfCompress
    return when (this) {
        CompressionProfile.HIGH_QUALITY -> p.profileHighQuality
        CompressionProfile.BALANCED -> p.profileBalanced
        CompressionProfile.MAXIMUM -> p.profileMaximum
    }
}

@Composable
fun CompressionProfile.localizedDescription(): String {
    val p = LocalAppStrings.current.pdfCompress
    return when (this) {
        CompressionProfile.HIGH_QUALITY -> p.profileHighQualityDesc
        CompressionProfile.BALANCED -> p.profileBalancedDesc
        CompressionProfile.MAXIMUM -> p.profileMaximumDesc
    }
}
