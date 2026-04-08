package ofis

import kotlin.test.Test
import kotlin.test.assertEquals
import ofis.ToolBox

class PdfCompressorTest {
    @Test
    fun testPdfCompressorProperties() {
        val compressor = PdfCompressor()
        assertEquals(ToolBox.PDF_COMPRESSOR, compressor.name)
        assertEquals("Compresses PDF files", compressor.description)
    }

    @Test
    fun testOutputPathGeneration() {
        val compressor = PdfCompressor()

        // Relative path
        assertEquals("compressed_test.pdf", compressor.getOutputPath("test.pdf", null))

        // Absolute path (Unix-like)
        assertEquals("/Users/test/compressed_file.pdf", compressor.getOutputPath("/Users/test/file.pdf", null))

        // Custom output
        assertEquals("custom.pdf", compressor.getOutputPath("test.pdf", "custom.pdf"))
    }

    // Future: Add more tests for parsing and compression logic
}
