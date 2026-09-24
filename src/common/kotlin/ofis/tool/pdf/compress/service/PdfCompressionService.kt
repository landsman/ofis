package ofis.tool.pdf.compress.service

import ofis.config.Logger
import ofis.platform.fileSystem
import ofis.platform.service.availableDiskSpace
import ofis.platform.service.findHelperBinary
import ofis.tool.pdf.compress.model.CompressionError
import ofis.tool.pdf.compress.model.CompressionProfile
import ofis.tool.pdf.compress.model.CompressionRequest
import ofis.tool.pdf.compress.model.CompressionResult
import ofis.tool.pdf.compress.model.NativeCommand
import ofis.tool.pdf.compress.model.ProcessResult
import okio.FileSystem
import okio.IOException
import okio.Path
import kotlin.time.measureTimedValue

class PdfCompressionService(
    private val binaryFinder: (String) -> String? = ::findHelperBinary,
    private val processRunner: (NativeCommand) -> ProcessResult = ::runProcess,
) {
    fun compress(request: CompressionRequest): CompressionResult =
        try {
            val (result, total) = measureTimedValue { doCompress(request, fileSystem) }
            Logger.info("[timing] total: $total")
            result
        } catch (e: IOException) {
            unexpected(e)
        } catch (e: IllegalStateException) {
            unexpected(e)
        }

    private fun unexpected(e: Exception): CompressionResult {
        Logger.info("[compress] unexpected exception: ${e.message}")
        return CompressionResult.Failure(CompressionError.InvalidInput("Unexpected error: ${e.message}"))
    }

    private fun doCompress(
        request: CompressionRequest,
        fs: FileSystem,
    ): CompressionResult =
        try {
            if (!fs.exists(request.inputPath)) {
                abort(CompressionError.InvalidInput("File not found: ${request.inputPath}"))
            }
            val originalBytes = fs.metadata(request.inputPath).size ?: 0L
            val warnings = mutableListOf<String>()

            val qpdfOutput = runQpdf(request, warnings)
            val finalTmp =
                when (request.profile) {
                    CompressionProfile.HIGH_QUALITY -> qpdfOutput
                    CompressionProfile.BALANCED, CompressionProfile.MAXIMUM -> runGhostscript(request, qpdfOutput)
                }
            finish(request, fs, finalTmp, originalBytes, warnings)
        } catch (e: Abort) {
            CompressionResult.Failure(e.reason)
        }

    // ── qpdf pass (always runs) ────────────────────────────────────────────────

    private fun runQpdf(
        request: CompressionRequest,
        warnings: MutableList<String>,
    ): Path {
        val qpdf = findBinary("qpdf") ?: abort(CompressionError.BinaryNotFound("qpdf"))
        val tmp = tmpPath(request, "qpdf")
        val result = runTimed("qpdf", buildQpdfCommand(qpdf, request.inputPath, tmp, request.profile))
        when (result.exitCode) {
            0 -> Unit
            QPDF_EXIT_WARNINGS -> warnings += "qpdf: completed with warnings"
            else -> {
                cleanupQuiet(tmp)
                abort(CompressionError.ProcessFailed("qpdf", result.exitCode, result.stderr))
            }
        }
        return tmp
    }

    // ── ghostscript pass (BALANCED and MAXIMUM only) ───────────────────────────

    private fun runGhostscript(
        request: CompressionRequest,
        input: Path,
    ): Path {
        val gs =
            findBinary("gs") ?: run {
                cleanupQuiet(input)
                abort(CompressionError.BinaryNotFound("gs (ghostscript)"))
            }
        val tmp = tmpPath(request, "gs")
        val result = runTimed("gs", buildGhostscriptCommand(gs, input, tmp, request.profile))
        cleanupQuiet(input)
        if (result.exitCode != 0) {
            cleanupQuiet(tmp)
            abort(CompressionError.ProcessFailed("gs", result.exitCode, result.stderr))
        }
        return tmp
    }

    // ── keep-smaller policy, then atomic move to final destination ─────────────

    private fun finish(
        request: CompressionRequest,
        fs: FileSystem,
        finalTmp: Path,
        originalBytes: Long,
        warnings: MutableList<String>,
    ): CompressionResult.Success {
        val compressedBytes = fs.metadata(finalTmp).size ?: 0L
        val keepOriginal = request.keepSmallerOnly && compressedBytes >= originalBytes
        if (keepOriginal) {
            cleanupQuiet(finalTmp)
            warnings += "Compressed output is not smaller than original — keeping original"
        } else {
            moveToOutput(request, fs, finalTmp, originalBytes)
        }
        return CompressionResult.Success(
            inputPath = request.inputPath,
            outputPath = if (keepOriginal) request.inputPath else request.outputPath,
            originalBytes = originalBytes,
            compressedBytes = if (keepOriginal) originalBytes else compressedBytes,
            profile = request.profile,
            warnings = warnings,
        )
    }

    private fun moveToOutput(
        request: CompressionRequest,
        fs: FileSystem,
        finalTmp: Path,
        originalBytes: Long,
    ) {
        try {
            request.outputPath.parent?.let { fs.createDirectories(it) }
            fs.atomicMove(finalTmp, request.outputPath)
        } catch (e: IOException) {
            cleanupQuiet(finalTmp)
            val outputDir = request.outputPath.parent?.toString() ?: "."
            val freeBytes = availableDiskSpace(outputDir)
            val requiredBytes = originalBytes * 2
            if (freeBytes < requiredBytes) {
                Logger.warn(
                    "Not enough disk space: need ${requiredBytes / BYTES_PER_MB}MB, " +
                        "only ${freeBytes / BYTES_PER_MB}MB free in $outputDir",
                )
                abort(CompressionError.FileSystemError("Not enough disk space to save the compressed file."))
            }
            Logger.warn("Failed to write output to ${request.outputPath}: ${e.message}")
            abort(CompressionError.FileSystemError("Could not save the file. Check folder permissions."))
        }
    }

    // ── step helpers ───────────────────────────────────────────────────────────

    private fun findBinary(name: String): String? {
        val (path, took) = measureTimedValue { binaryFinder(name) }
        Logger.info("[timing] find $name: $took → $path")
        return path
    }

    private fun runTimed(
        label: String,
        command: NativeCommand,
    ): ProcessResult {
        val (result, took) = measureTimedValue { processRunner(command) }
        Logger.info("[timing] $label: $took (exit=${result.exitCode})")
        return result
    }

    private fun tmpPath(
        request: CompressionRequest,
        pass: String,
    ): Path = request.outputPath.parent!! / "${request.outputPath.name}.$pass.tmp"

    // ── helpers ────────────────────────────────────────────────────────────────

    private fun cleanupQuiet(path: Path) {
        try {
            fileSystem.delete(path)
        } catch (_: Exception) {
        }
    }

    private companion object {
        /** qpdf finished but reported warnings; the output is usable. */
        const val QPDF_EXIT_WARNINGS = 3
        const val BYTES_PER_MB = 1_048_576L
    }
}

/** A step failed in an expected way; `doCompress` turns it into a [CompressionResult.Failure]. */
private class Abort(
    val reason: CompressionError,
) : Exception(reason.toString())

private fun abort(reason: CompressionError): Nothing = throw Abort(reason)
