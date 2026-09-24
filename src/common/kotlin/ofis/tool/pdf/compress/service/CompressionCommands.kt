package ofis.tool.pdf.compress.service

import ofis.tool.pdf.compress.model.CompressionProfile
import ofis.tool.pdf.compress.model.NativeCommand
import okio.Path

internal fun buildQpdfCommand(
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

internal fun buildGhostscriptCommand(
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
