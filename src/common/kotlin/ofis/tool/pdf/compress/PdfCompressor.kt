package ofis.tool.pdf.compress

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ofis.config.Logger
import ofis.i18n.LocalAppStrings
import ofis.tool.Tool
import ofis.tool.ToolBox
import ofis.tool.ToolInputSpec
import ofis.tool.ToolRegistry
import ofis.tool.pdf.compress.model.CompressionProfile
import ofis.tool.pdf.compress.model.CompressionRequest
import ofis.tool.pdf.compress.model.CompressionResult
import ofis.tool.pdf.compress.service.PdfCompressionService
import ofis.tool.pdf.compress.ui.PdfDetailScreen
import ofis.ui.component.DetailView
import ofis.ui.system.LocalLogController
import ofis.utils.format.formatSize
import okio.Path.Companion.toPath

class PdfCompressor : Tool {
    override val name = ToolBox.PDF_COMPRESSOR
    override val displayName = "Compress PDF"
    override val description = "Reduce file size while keeping your document readable."
    override val inputSpecs =
        listOf(
            ToolInputSpec(extensions = setOf("pdf"), label = "PDF document"),
        )

    private val service = PdfCompressionService()

    @Composable
    override fun localizedDisplayName(): String = LocalAppStrings.current.pdfCompressName

    @Composable
    override fun localizedDescription(): String = LocalAppStrings.current.pdfCompressDescription

    @Composable
    override fun Screen(onBack: () -> Unit) {
        val logController = LocalLogController.current
        DetailView(
            title = localizedDisplayName(),
            onBack = onBack,
            headerActions = {
                IconButton(onClick = { logController.toggle() }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Toggle logs", // todo: localize!
                        tint = if (logController.isVisible) MaterialTheme.colorScheme.primary else Color.Gray,
                    )
                }
            },
        ) {
            PdfDetailScreen(tool = this)
        }
    }

    override fun register() {
        ToolRegistry.register(this)
    }

    override fun run(args: List<String>) {
        if (args.isEmpty()) {
            Logger.info("Usage: ${ToolBox.PDF_COMPRESSOR} <input.pdf> [output.pdf] [--profile high|balanced|max]")
            return
        }

        val inputStr = args[0]
        val outputStr = args.getOrNull(1)?.takeIf { !it.startsWith("--") } ?: defaultOutput(inputStr)
        val profile = parseProfile(args)

        val request =
            CompressionRequest(
                inputPath = inputStr.toPath(),
                outputPath = outputStr.toPath(),
                profile = profile,
            )

        Logger.info("Compressing $inputStr → $outputStr [${profile.label}]")

        when (val result = service.compress(request)) {
            is CompressionResult.Success -> {
                result.warnings.forEach { Logger.warn(it) }
                if (result.alreadyOptimal) {
                    Logger.info("Already optimized — compressed file is not smaller")
                } else {
                    Logger.info("Done. Saved ${formatSize(result.savedBytes)} (${result.savedPercent}%)")
                    Logger.info("OUTPUT_PATH: ${result.outputPath}")
                    Logger.info("SUGGESTED_NAME: ${suggestedName(inputStr, profile)}")
                }
                Logger.info(
                    "RESIZE_INFO: ${formatSize(result.originalBytes)} → ${formatSize(result.compressedBytes)} (${result.savedPercent}%)",
                )
            }
            is CompressionResult.Failure -> {
                Logger.info("Error: ${result.reason}")
            }
        }
    }

    private fun suggestedName(
        input: String,
        profile: CompressionProfile,
    ): String {
        val name = input.toPath().name
        val base = name.substringBeforeLast(".")
        val suffix =
            when (profile) {
                CompressionProfile.HIGH_QUALITY -> "high-quality"
                CompressionProfile.BALANCED -> "balanced"
                CompressionProfile.MAXIMUM -> "maximum"
            }
        return "$base-ofis-compressed-$suffix.pdf"
    }

    private fun defaultOutput(input: String): String {
        val p = input.toPath()
        val parent = p.parent
        return if (parent != null) {
            (parent / "compressed_${p.name}").toString()
        } else {
            "compressed_${p.name}"
        }
    }

    private fun parseProfile(args: List<String>): CompressionProfile {
        val idx = args.indexOf("--profile")
        if (idx != -1 && idx + 1 < args.size) {
            return when (args[idx + 1].lowercase()) {
                "high" -> CompressionProfile.HIGH_QUALITY
                "max", "maximum" -> CompressionProfile.MAXIMUM
                else -> CompressionProfile.BALANCED
            }
        }
        return CompressionProfile.BALANCED
    }
}
