package ofis.tool.image.png.optimize.service

import ofis.platform.fileSystem
import ofis.platform.service.findHelperBinary
import ofis.tool.pdf.compress.model.CompressionError
import ofis.tool.pdf.compress.model.CompressionProfile
import ofis.tool.pdf.compress.model.CompressionResult
import ofis.tool.pdf.compress.model.NativeCommand
import ofis.tool.pdf.compress.model.ProcessResult
import ofis.tool.pdf.compress.service.runProcess
import okio.IOException
import okio.Path

class PngOptimizeService(
    private val binaryFinder: (String) -> String? = ::findHelperBinary,
    private val processRunner: (NativeCommand) -> ProcessResult = ::runProcess,
) {
    fun optimize(
        input: Path,
        output: Path,
    ): CompressionResult =
        try {
            when {
                // The not-smaller branch deletes the output, which here would be the only copy.
                input == output -> failure(CompressionError.InvalidInput("Output must differ from the input: $input"))

                !fileSystem.exists(input) -> failure(CompressionError.InvalidInput("File not found: $input"))

                else ->
                    run(
                        binaryFinder(OXIPNG) ?: return failure(CompressionError.BinaryNotFound(OXIPNG)),
                        input,
                        output,
                    )
            }
        } catch (e: IOException) {
            failure(CompressionError.FileSystemError("Could not write the file: ${e.message}"))
        }

    private fun run(
        binary: String,
        input: Path,
        output: Path,
    ): CompressionResult {
        val originalBytes = fileSystem.metadata(input).size ?: 0L
        val result = processRunner(buildOxipngCommand(binary, input, output))
        if (result.exitCode != 0) {
            fileSystem.delete(output, mustExist = false)
            return failure(CompressionError.ProcessFailed(OXIPNG, result.exitCode, result.stderr))
        }
        val optimizedBytes = fileSystem.metadata(output).size ?: 0L
        val keepOriginal = optimizedBytes >= originalBytes
        if (keepOriginal) fileSystem.delete(output)
        return CompressionResult.Success(
            inputPath = input,
            outputPath = if (keepOriginal) input else output,
            originalBytes = originalBytes,
            compressedBytes = if (keepOriginal) originalBytes else optimizedBytes,
            // ponytail: lossless, so the PDF result type fits; own type at the first lossy mode
            profile = CompressionProfile.HIGH_QUALITY,
        )
    }

    private fun failure(reason: CompressionError) = CompressionResult.Failure(reason)

    private companion object {
        const val OXIPNG = "oxipng"
    }
}

/** `--strip safe` keeps the colour profile and physical size; text and EXIF, orientation included, go. */
internal fun buildOxipngCommand(
    binary: String,
    input: Path,
    output: Path,
): NativeCommand =
    NativeCommand(
        executable = binary,
        arguments = listOf("-o", "4", "--strip", "safe", "--out", output.toString(), "--", input.toString()),
    )
