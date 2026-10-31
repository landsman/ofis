package ofis.tool.image.png.optimize

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
import ofis.tool.image.png.optimize.service.PngOptimizeService
import ofis.tool.image.png.optimize.ui.PngDetailScreen
import ofis.tool.pdf.compress.model.CompressionResult
import ofis.ui.component.DetailView
import ofis.ui.system.LocalLogController
import ofis.utils.format.formatSize
import okio.Path.Companion.toPath

class PngOptimizer : Tool {
    override val name = ToolBox.PNG_OPTIMIZER
    override val displayName = "Optimize PNG"
    override val description = "Make a PNG smaller without changing a single pixel."
    override val inputSpecs =
        listOf(
            ToolInputSpec(extensions = setOf("png"), label = "PNG image"),
        )

    private val service = PngOptimizeService()

    @Composable
    override fun localizedDisplayName(): String = LocalAppStrings.current.pngOptimizeName

    @Composable
    override fun localizedDescription(): String = LocalAppStrings.current.pngOptimizeDescription

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
                        contentDescription = LocalAppStrings.current.logsTitle,
                        tint = if (logController.isVisible) MaterialTheme.colorScheme.primary else Color.Gray,
                    )
                }
            },
        ) {
            PngDetailScreen(tool = this)
        }
    }

    override fun register() {
        ToolRegistry.register(this)
    }

    override fun run(args: List<String>) {
        if (args.isEmpty()) {
            Logger.info("Usage: ${ToolBox.PNG_OPTIMIZER} <input.png> [output.png]")
            return
        }

        val input = args[0].toPath()
        val outputName = "compressed_${input.name}"
        val output = args.getOrNull(1)?.toPath() ?: input.parent?.div(outputName) ?: outputName.toPath()

        Logger.info("Optimizing $input → $output")

        when (val result = service.optimize(input, output)) {
            is CompressionResult.Success -> {
                if (result.alreadyOptimal) {
                    Logger.info("Already optimized — output is not smaller")
                } else {
                    Logger.info("Done. Saved ${formatSize(result.savedBytes)} (${result.savedPercent}%)")
                    Logger.info("OUTPUT_PATH: ${result.outputPath}")
                    Logger.info("SUGGESTED_NAME: ${input.name.substringBeforeLast(".")}-ofis-optimized.png")
                }
                val sizes = "${formatSize(result.originalBytes)} → ${formatSize(result.compressedBytes)}"
                Logger.info("RESIZE_INFO: $sizes (${result.savedPercent}%)")
            }

            is CompressionResult.Failure -> {
                Logger.info("Error: ${result.reason}")
            }
        }
    }
}
