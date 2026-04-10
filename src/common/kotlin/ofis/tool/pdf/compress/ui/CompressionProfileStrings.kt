package ofis.tool.pdf.compress.ui

import androidx.compose.runtime.Composable
import ofis.generated.resources.Res
import ofis.generated.resources.profile_balanced
import ofis.generated.resources.profile_balanced_desc
import ofis.generated.resources.profile_high_quality
import ofis.generated.resources.profile_high_quality_desc
import ofis.generated.resources.profile_maximum
import ofis.generated.resources.profile_maximum_desc
import ofis.tool.pdf.compress.model.CompressionProfile
import org.jetbrains.compose.resources.stringResource

@Composable
fun CompressionProfile.localizedLabel(): String =
    when (this) {
        CompressionProfile.HIGH_QUALITY -> stringResource(Res.string.profile_high_quality)
        CompressionProfile.BALANCED -> stringResource(Res.string.profile_balanced)
        CompressionProfile.MAXIMUM -> stringResource(Res.string.profile_maximum)
    }

@Composable
fun CompressionProfile.localizedDescription(): String =
    when (this) {
        CompressionProfile.HIGH_QUALITY -> stringResource(Res.string.profile_high_quality_desc)
        CompressionProfile.BALANCED -> stringResource(Res.string.profile_balanced_desc)
        CompressionProfile.MAXIMUM -> stringResource(Res.string.profile_maximum_desc)
    }
