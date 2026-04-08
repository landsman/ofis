package ofis

import okio.Path.Companion.toPath
import okio.buffer

class PdfCompressor : Tool {
    override val name = ToolBox.PDF_COMPRESSOR
    override val description = "Compresses PDF files"

    override fun run(args: List<String>) {
        if (args.isEmpty()) {
            Logger.info("Usage: ${ToolBox.PDF_COMPRESSOR} <input.pdf> [output.pdf] [--level <1-9>]")
            return
        }
        val input = args[0]
        val output = getOutputPath(input, args.getOrNull(1))
        val level = args.getOrElse(args.indexOf("--level") + 1) { "5" }.toIntOrNull() ?: 5

        Logger.info("Compressing $input to $output with level $level...")

        compressPdf(input, output, level)
    }

    internal fun getOutputPath(
        input: String,
        providedOutput: String?,
    ): String {
        if (providedOutput != null) return providedOutput
        val inputPath = input.toPath()
        val parent = inputPath.parent
        return if (parent != null) {
            (parent / "compressed_${inputPath.name}").toString()
        } else {
            "compressed_${inputPath.name}"
        }
    }

    private fun compressPdf(
        input: String,
        output: String,
        level: Int,
    ) {
        val fs = fileSystem
        val inputPath = input.toPath()
        val outputPath = output.toPath()

        if (!fs.exists(inputPath)) {
            Logger.info("Error: Input file $input not found.")
            return
        }

        try {
            // 1. Analyze
            val source = fs.source(inputPath).buffer()
            try {
                val parser = PdfParser(source)
                parser.analyze()
            } finally {
                source.close()
            }

            // 2. Compress/Write (For now just copy)
            val sink = fs.sink(outputPath).buffer()
            try {
                val source = fs.source(inputPath).buffer()
                try {
                    sink.writeAll(source)
                } finally {
                    source.close()
                }
                sink.flush()
            } finally {
                sink.close()
            }

            Logger.info("Saved compressed (not really) PDF to $output")
        } catch (e: Exception) {
            Logger.info("Error during compression: ${e.message}")
        }
    }
}

fun registerPdfTool() {
    ToolRegistry.register(PdfCompressor())
}
