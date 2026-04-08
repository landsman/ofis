package ofis

import kotlin.test.Test
import kotlin.test.assertEquals
import ofis.tool.ToolBox
import ofis.tool.pdf.compress.PdfCompressor
import ofis.utils.format.formatSize

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

        // todo: fix this
        // Relative path
        //assertEquals("compressed_test.pdf", compressor.getOutputPath("test.pdf", null))

        // Absolute path (Unix-like)
        //assertEquals("/Users/test/compressed_file.pdf", compressor.getOutputPath("/Users/test/file.pdf", null))

        // Custom output
        //assertEquals("custom.pdf", compressor.getOutputPath("test.pdf", "custom.pdf"))
    }

    @Test
    fun testFormatSize() {
        assertEquals("500 B",
                     formatSize(500)
        )
        assertEquals("1 KB",
                     formatSize(1024)
        )
        assertEquals("1 MB",
                     formatSize(1024 * 1024)
        )
        assertEquals("10 MB",
                     formatSize(10 * 1024 * 1024)
        )
    }

    // Future: Add more tests for parsing and compression logic
}
