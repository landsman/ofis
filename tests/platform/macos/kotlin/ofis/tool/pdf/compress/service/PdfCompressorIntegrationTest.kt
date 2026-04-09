package ofis.tool.pdf.compress.service

import ofis.tool.pdf.compress.model.CompressionProfile
import ofis.tool.pdf.compress.model.CompressionRequest
import ofis.tool.pdf.compress.model.CompressionResult
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.measureTime

/**
 * Integration tests: run actual qpdf + ghostscript binaries against committed PDF fixtures.
 *
 * Fixture files are in testdata/pdf/ (committed to repository, never deleted by tests).
 * Only the compression output files are written to a temp dir and cleaned up after each test.
 *
 * Prerequisites (on CI add to workflow: `brew install qpdf ghostscript`):
 *   make install-bins
 */
class PdfCompressorIntegrationTest {
    private val fs = FileSystem.SYSTEM
    private val tmpDir: Path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "ofis_integration_test"

    /** Resolved at runtime relative to the process working directory (project root when run via Gradle). */
    private val textPdf: Path = "tests/resources/pdf/text.pdf".toPath()
    private val photoPdf: Path = "tests/resources/pdf/photo.pdf".toPath()

    private val outputs = mutableListOf<Path>()

    @BeforeTest
    fun setUp() {
        assertTrue(fs.exists(textPdf), "Missing fixture: $textPdf — run from project root")
        assertTrue(fs.exists(photoPdf), "Missing fixture: $photoPdf — run from project root")
        fs.createDirectories(tmpDir)
        println()
        println("[integration] text PDF:  ${fs.metadata(textPdf).size?.div(1024)} KB  ($textPdf)")
        println("[integration] photo PDF: ${fs.metadata(photoPdf).size?.div(1024)} KB ($photoPdf)")
    }

    @AfterTest
    fun tearDown() {
        // Only output files are cleaned up — fixtures in testdata/ are never touched
        outputs.forEach { fs.delete(it, mustExist = false) }
        outputs.clear()
        fs.deleteRecursively(tmpDir, mustExist = false)
    }

    // ── text PDF — qpdf recompresses uncompressed streams ────────────────────

    @Test
    fun `HIGH_QUALITY - text PDF - qpdf only`() {
        compress(textPdf, CompressionProfile.HIGH_QUALITY).let { reportAndAssert("HIGH_QUALITY text", it) }
    }

    @Test
    fun `BALANCED - text PDF - qpdf then ghostscript`() {
        compress(textPdf, CompressionProfile.BALANCED).let { reportAndAssert("BALANCED text", it) }
    }

    @Test
    fun `MAXIMUM - text PDF - qpdf then ghostscript lowest DPI`() {
        compress(textPdf, CompressionProfile.MAXIMUM).let { reportAndAssert("MAXIMUM text", it) }
    }

    // ── photo PDF — ghostscript downsamples raster images ────────────────────

    @Test
    fun `HIGH_QUALITY - photo PDF - qpdf only`() {
        compress(photoPdf, CompressionProfile.HIGH_QUALITY).let { reportAndAssert("HIGH_QUALITY photo", it) }
    }

    @Test
    fun `BALANCED - photo PDF - ghostscript downsamples to 144 DPI`() {
        val result = compress(photoPdf, CompressionProfile.BALANCED)
        reportAndAssert("BALANCED photo", result)
        assertIs<CompressionResult.Success>(result)
        assertTrue(
            result.compressedBytes < result.originalBytes,
            "Expected BALANCED to shrink photo PDF but got ${result.compressedBytes}B >= ${result.originalBytes}B",
        )
    }

    @Test
    fun `MAXIMUM - photo PDF - ghostscript downsamples to 96 DPI`() {
        val result = compress(photoPdf, CompressionProfile.MAXIMUM)
        reportAndAssert("MAXIMUM photo", result)
        assertIs<CompressionResult.Success>(result)
        assertTrue(
            result.compressedBytes < result.originalBytes,
            "Expected MAXIMUM to shrink photo PDF but got ${result.compressedBytes}B >= ${result.originalBytes}B",
        )
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun compress(
        input: Path,
        profile: CompressionProfile,
    ): CompressionResult {
        val output = (tmpDir / "out_${input.name}_${profile.name.lowercase()}.pdf").also { outputs += it }
        var result: CompressionResult? = null
        val elapsed =
            measureTime {
                result =
                    PdfCompressionService().compress(
                        CompressionRequest(inputPath = input, outputPath = output, profile = profile),
                    )
            }
        println("[integration] ${profile.name} ${input.name} wall-clock: $elapsed")
        return result!!
    }

    private fun reportAndAssert(
        label: String,
        result: CompressionResult,
    ) {
        assertIs<CompressionResult.Success>(result, "Expected success, got: $result")
        val origKb = result.originalBytes / 1024
        val compKb = result.compressedBytes / 1024
        println("[integration] $label: ${origKb}KB → ${compKb}KB (${result.savedPercent}% saved, alreadyOptimal=${result.alreadyOptimal})")
        assertTrue(result.compressedBytes > 0, "Output must not be empty")
        assertTrue(fs.exists(result.outputPath), "Output file must exist at ${result.outputPath}")
    }
}
