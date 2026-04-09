package ofis.tool.pdf.compress.service

import ofis.config.Logger
import ofis.platform.fileSystem
import ofis.platform.service.findHelperBinary
import ofis.tool.pdf.compress.model.CompressionError
import ofis.tool.pdf.compress.model.CompressionProfile
import ofis.tool.pdf.compress.model.CompressionRequest
import ofis.tool.pdf.compress.model.CompressionResult
import ofis.tool.pdf.compress.model.NativeCommand
import ofis.tool.pdf.compress.model.ProcessResult
import okio.Path
import kotlin.time.measureTime

class PdfCompressionService(
    private val binaryFinder: (String) -> String? = ::findHelperBinary,
    private val processRunner: (NativeCommand) -> ProcessResult = ::runProcess,
) {
    fun compress(request: CompressionRequest): CompressionResult {
        val fs = fileSystem
        var result: CompressionResult? = null
        return try {
            val total =
                measureTime {
                    result = doCompress(request, fs)
                }
            Logger.info("[timing] total: $total")
            result!!
        } catch (e: Exception) {
            Logger.info("[compress] unexpected exception: ${e.message}")
            CompressionResult.Failure(CompressionError.InvalidInput("Unexpected error: ${e.message}"))
        }
    }

    private fun doCompress(
        request: CompressionRequest,
        fs: okio.FileSystem,
    ): CompressionResult {
        // ── validate input ─────────────────────────────────────────────────
        if (!fs.exists(request.inputPath)) {
            return CompressionResult.Failure(CompressionError.InvalidInput("File not found: ${request.inputPath}"))
        }

        val originalBytes = fs.metadata(request.inputPath).size ?: 0L

        // ── resolve binaries ───────────────────────────────────────────────
        lateinit var qpdf: String
        val tFindQpdf =
            measureTime {
                qpdf =
                    binaryFinder("qpdf") ?: return CompressionResult.Failure(CompressionError.BinaryNotFound("qpdf"))
            }
        Logger.info("[timing] find qpdf: $tFindQpdf → $qpdf")

        val tmpQpdf: Path = request.outputPath.parent!! / "${request.outputPath.name}.qpdf.tmp"

        // ── qpdf pass (always runs) ────────────────────────────────────────
        lateinit var qpdfResult: ProcessResult
        val tQpdf = measureTime { qpdfResult = processRunner(buildQpdfCommand(qpdf, request.inputPath, tmpQpdf, request.profile)) }
        Logger.info("[timing] qpdf: $tQpdf (exit=${qpdfResult.exitCode})")

        if (qpdfResult.exitCode != 0 && qpdfResult.exitCode != 3) {
            cleanupQuiet(tmpQpdf)
            return CompressionResult.Failure(
                CompressionError.ProcessFailed("qpdf", qpdfResult.exitCode, qpdfResult.stderr),
            )
        }
        val warnings =
            buildList {
                if (qpdfResult.exitCode == 3) add("qpdf: completed with warnings")
            }.toMutableList()

        // ── ghostscript pass (BALANCED and MAXIMUM only) ───────────────────
        val finalTmp: Path =
            when (request.profile) {
                CompressionProfile.HIGH_QUALITY -> tmpQpdf

                CompressionProfile.BALANCED, CompressionProfile.MAXIMUM -> {
                    lateinit var gs: String
                    val tFindGs =
                        measureTime {
                            gs =
                                binaryFinder("gs")
                                    ?: return run {
                                        cleanupQuiet(tmpQpdf)
                                        CompressionResult.Failure(CompressionError.BinaryNotFound("gs (ghostscript)"))
                                    }
                        }
                    Logger.info("[timing] find gs: $tFindGs → $gs")

                    val tmpGs: Path = request.outputPath.parent!! / "${request.outputPath.name}.gs.tmp"
                    lateinit var gsResult: ProcessResult
                    val tGs = measureTime { gsResult = processRunner(buildGhostscriptCommand(gs, tmpQpdf, tmpGs, request.profile)) }
                    Logger.info("[timing] gs: $tGs (exit=${gsResult.exitCode})")
                    cleanupQuiet(tmpQpdf)

                    if (gsResult.exitCode != 0) {
                        cleanupQuiet(tmpGs)
                        return CompressionResult.Failure(
                            CompressionError.ProcessFailed("gs", gsResult.exitCode, gsResult.stderr),
                        )
                    }
                    tmpGs
                }
            }

        val compressedBytes = fs.metadata(finalTmp).size ?: 0L

        // ── keep-smaller policy ────────────────────────────────────────────
        if (request.keepSmallerOnly && compressedBytes >= originalBytes) {
            cleanupQuiet(finalTmp)
            warnings += "Compressed output is not smaller than original — keeping original"
            return CompressionResult.Success(
                inputPath = request.inputPath,
                outputPath = request.inputPath,
                originalBytes = originalBytes,
                compressedBytes = originalBytes,
                profile = request.profile,
                warnings = warnings,
            )
        }

        // ── atomic move to final destination ───────────────────────────────
        request.outputPath.parent?.let { fs.createDirectories(it) }
        fs.atomicMove(finalTmp, request.outputPath)

        return CompressionResult.Success(
            inputPath = request.inputPath,
            outputPath = request.outputPath,
            originalBytes = originalBytes,
            compressedBytes = compressedBytes,
            profile = request.profile,
            warnings = warnings,
        )
    }

    // ── command builders ───────────────────────────────────────────────────────

    private fun buildQpdfCommand(
        binary: String,
        input: Path,
        output: Path,
        profile: CompressionProfile,
    ): NativeCommand {
        val level =
            when (profile) {
                CompressionProfile.HIGH_QUALITY -> "9"
                CompressionProfile.BALANCED -> "6"
                CompressionProfile.MAXIMUM -> "9"
            }
        return NativeCommand(
            executable = binary,
            arguments =
                listOf(
                    input.toString(),
                    "--compress-streams=y",
                    "--object-streams=generate",
                    "--recompress-flate",
                    "--compression-level=$level",
                    output.toString(),
                ),
        )
    }

    private fun buildGhostscriptCommand(
        binary: String,
        input: Path,
        output: Path,
        profile: CompressionProfile,
    ): NativeCommand {
        val preset =
            when (profile) {
                CompressionProfile.BALANCED -> "/ebook"
                CompressionProfile.MAXIMUM -> "/screen"
                CompressionProfile.HIGH_QUALITY -> error("gs not used for HIGH_QUALITY")
            }
        val dpi =
            when (profile) {
                CompressionProfile.BALANCED -> 144
                CompressionProfile.MAXIMUM -> 96
                CompressionProfile.HIGH_QUALITY -> error("gs not used for HIGH_QUALITY")
            }
        return NativeCommand(
            executable = binary,
            arguments =
                listOf(
                    "-q",
                    "-dNOPAUSE",
                    "-dBATCH",
                    "-dSAFER",
                    "-sDEVICE=pdfwrite",
                    "-dCompatibilityLevel=1.4",
                    "-dPDFSETTINGS=$preset",
                    "-dColorImageDownsampleType=/Bicubic",
                    "-dColorImageResolution=$dpi",
                    "-dGrayImageDownsampleType=/Bicubic",
                    "-dGrayImageResolution=$dpi",
                    "-dMonoImageDownsampleType=/Bicubic",
                    "-dMonoImageResolution=$dpi",
                    "-sOutputFile=$output",
                    input.toString(),
                ),
        )
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private fun cleanupQuiet(path: Path) {
        try {
            fileSystem.delete(path)
        } catch (_: Exception) {
        }
    }
}
