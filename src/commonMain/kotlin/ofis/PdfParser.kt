package ofis

import okio.BufferedSource
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8

class PdfParser(
    private val source: BufferedSource,
) {
    private var content: ByteString? = null

    fun analyze() {
        if (content == null) {
            content = source.readByteString()
        }
        val bytes = content ?: return
        Logger.debug("Analyzing PDF content (${bytes.size} bytes)...")

        if (bytes.size < 10) {
            Logger.info("File too small to be a PDF")
            return
        }

        val header = bytes.substring(0, 10).utf8()
        Logger.debug("PDF Header (starts with): ${header.takeWhile { it != '\n' && it != '\r' }}")

        if (!header.startsWith("%PDF-")) {
            Logger.info("Not a valid PDF file")
            return
        }

        // Search for trailer and startxref from the end
        val trailerIndex = bytes.lastIndexOf("trailer".encodeUtf8())
        val startXrefIndex = bytes.lastIndexOf("startxref".encodeUtf8())

        if (trailerIndex != -1) {
            Logger.debug("Found 'trailer' at offset: $trailerIndex")
        } else {
            Logger.debug("Trailer keyword not found")
        }

        if (startXrefIndex != -1) {
            Logger.debug("Found 'startxref' at offset: $startXrefIndex")
        } else {
            Logger.debug("startxref keyword not found")
        }
    }
}
