package ofis

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class ToolRegistryTest {
    @Test
    fun testRegistryHasPdfCompressor() {
        // Force initialization if needed
        // Since we are using top-level initialization in PdfCompressor.kt,
        // it might not be initialized yet unless something references it.
        // Let's check.
        
        val pdfTool = ToolRegistry.get("pdf-compress")
        assertNotNull(pdfTool, "PdfCompressor should be registered in ToolRegistry")
    }

    @Test
    fun testListTools() {
        val tools = ToolRegistry.list()
        assertTrue(tools.isNotEmpty(), "ToolRegistry should not be empty")
        assertTrue(tools.any { it.name == "pdf-compress" }, "ToolRegistry should contain pdf-compress")
    }
}
