package ofis.tool.pdf.compress.ui

import ofis.tool.pdf.compress.model.CompressionProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PdfDetailStateTest {
    @Test
    fun `result lines fill the state and stop the run`() {
        val state = PdfDetailState()
        state.selectFile("/tmp/a.pdf", 100)
        state.startCompression(CompressionProfile.MAXIMUM)
        assertTrue(state.isRunning)

        assertNull(state.onLogMessage("OUTPUT_PATH: /tmp/out.pdf"))
        assertNull(state.onLogMessage("SUGGESTED_NAME: a-ofis.pdf"))
        assertNull(state.onLogMessage("RESIZE_INFO: 200 KB → 100 KB (50%)"))

        assertFalse(state.isRunning)
        assertEquals("/tmp/out.pdf", state.outputFilePath)
        assertEquals("a-ofis.pdf", state.suggestedSaveName)
        assertEquals(CompressionProfile.MAXIMUM, state.completedProfile)
        assertFalse(state.isAlreadyOptimal)
    }

    @Test
    fun `zero percent saved is already optimal`() {
        val state = PdfDetailState()
        state.onLogMessage("RESIZE_INFO: 100 KB → 100 KB (0%)")
        assertTrue(state.isAlreadyOptimal)
    }

    @Test
    fun `error line stops the run and returns the message`() {
        val state = PdfDetailState()
        state.startCompression(CompressionProfile.BALANCED)
        assertEquals("qpdf failed", state.onLogMessage("Error: qpdf failed"))
        assertFalse(state.isRunning)
    }

    @Test
    fun `selecting a new file clears the previous result`() {
        val state = PdfDetailState()
        state.onLogMessage("RESIZE_INFO: 200 KB → 100 KB (50%)")
        state.onLogMessage("OUTPUT_PATH: /tmp/out.pdf")
        state.selectFile("/tmp/b.pdf", 5)
        assertNull(state.resizeInfo)
        assertNull(state.outputFilePath)
        assertTrue(state.showProfileSelector)
    }
}
