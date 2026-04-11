package ofis.tool.pdf.compress.ui

import androidx.compose.runtime.Composable
import ofis.i18n.LocalAppStrings
import ofis.tool.pdf.compress.model.CompressionProfile

@Composable
fun CompressionProfile.localizedLabel(): String {
    val strings = LocalAppStrings.current
    return when (this) {
        CompressionProfile.HIGH_QUALITY -> strings.profileHighQuality
        CompressionProfile.BALANCED -> strings.profileBalanced
        CompressionProfile.MAXIMUM -> strings.profileMaximum
    }
}

@Composable
fun CompressionProfile.localizedDescription(): String {
    val strings = LocalAppStrings.current
    return when (this) {
        CompressionProfile.HIGH_QUALITY -> strings.profileHighQualityDesc
        CompressionProfile.BALANCED -> strings.profileBalancedDesc
        CompressionProfile.MAXIMUM -> strings.profileMaximumDesc
    }
}
