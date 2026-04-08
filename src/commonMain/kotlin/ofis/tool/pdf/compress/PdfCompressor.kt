package ofis.tool.pdf.compress

import ofis.ToolRegistry
import ofis.config.Logger
import ofis.tool.Tool
import ofis.tool.ToolBox
import ofis.utils.format.formatSize
import okio.Path.Companion.toPath

class PdfCompressor : Tool {
    override val name = ToolBox.PDF_COMPRESSOR
    override val description = "Compresses PDF files"

    private val service = PdfCompressionService()

    override fun run(args: List<String>) {
        if (args.isEmpty()) {
            Logger.info("Usage: ${ToolBox.PDF_COMPRESSOR} <input.pdf> [output.pdf] [--profile high|balanced|max]")
            return
        }

        val inputStr = args[0]
        val outputStr = args.getOrNull(1)?.takeIf { !it.startsWith("--") } ?: defaultOutput(inputStr)
        val profile = parseProfile(args)

        val request = CompressionRequest(
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
                Logger.info("RESIZE_INFO: ${formatSize(result.originalBytes)} → ${formatSize(result.compressedBytes)} (${result.savedPercent}%)")
            }
            is CompressionResult.Failure -> {
                Logger.info("Error: ${result.reason}")
            }
        }
    }

    private fun suggestedName(input: String, profile: CompressionProfile): String {
        val name = input.toPath().name
        val base = name.substringBeforeLast(".")
        val suffix = when (profile) {
            CompressionProfile.HIGH_QUALITY -> "high-quality"
            CompressionProfile.BALANCED     -> "balanced"
            CompressionProfile.MAXIMUM      -> "maximum"
        }
        return "$base-ofis-compressed-$suffix.pdf"
    }

    private fun defaultOutput(input: String): String {
        val p = input.toPath()
        val parent = p.parent
        return if (parent != null) (parent / "compressed_${p.name}").toString()
        else "compressed_${p.name}"
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

fun registerPdfTool() {
    ToolRegistry.register(PdfCompressor())
}
