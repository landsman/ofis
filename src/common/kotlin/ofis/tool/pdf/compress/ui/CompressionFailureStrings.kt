package ofis.tool.pdf.compress.ui

import ofis.i18n.AppStrings
import ofis.tool.pdf.compress.model.CompressionFailureKind

fun AppStrings.compressionFailureMessage(kind: CompressionFailureKind): String =
    when (kind) {
        CompressionFailureKind.UNREADABLE_FILE -> pdfCompressErrorUnreadableFile
        CompressionFailureKind.FILE_NOT_FOUND -> pdfCompressErrorFileNotFound
        CompressionFailureKind.TOOL_MISSING -> pdfCompressErrorToolMissing
        CompressionFailureKind.NOT_ENOUGH_SPACE -> pdfCompressNotEnoughSpace
        CompressionFailureKind.CANNOT_WRITE -> pdfCompressErrorCannotWrite
        CompressionFailureKind.UNEXPECTED -> pdfCompressErrorUnexpected
    }
