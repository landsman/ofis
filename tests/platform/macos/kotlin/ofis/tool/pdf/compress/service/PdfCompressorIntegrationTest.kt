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
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.measureTime

/**
 * Integration tests: run actual qpdf + ghostscript binaries against committed PDF fixtures.
 *
 * Fixture files are in tests/resources/pdf/ (committed to repository, never deleted by tests).
 * Only the compression output files are written to a temp dir and cleaned up after each test.
 *
 * If a fixture is intentionally replaced, update the expected size constants below.
 *
 * Prerequisites (on CI add to workflow: `brew install qpdf ghostscript`):
 *   make install-bins
 */
class PdfCompressorIntegrationTest {
    private val fs = FileSystem.SYSTEM
    private val tmpDir: Path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "ofis_integration_test"

    // ── Update when fixtures or qpdf/gs version change intentionally ─────────
    private data class Fixture(
        val path: Path,
        val originalBytes: Long,
        val highQualityRange: LongRange,
        val balancedRange: LongRange,
        val maximumRange: LongRange,
    )

    private val textPdf =
        Fixture(
            path = "tests/resources/pdf/text.pdf".toPath(),
            originalBytes = 12_563L,
            highQualityRange = 500L..3_000L,
            balancedRange = 3_000L..8_000L,
            maximumRange = 3_000L..8_000L,
        )

    private val photoPdf =
        Fixture(
            path = "tests/resources/pdf/testing-document-lossless.pdf".toPath(),
            originalBytes = 2_863_518L,
            highQualityRange = 2_500_000L..3_000_000L, // qpdf only, barely changes
            balancedRange = 80_000L..220_000L, // gs 144 DPI
            maximumRange = 40_000L..130_000L, // gs 96 DPI
        )
    // ─────────────────────────────────────────────────────────────────────────

    private val outputs = mutableListOf<Path>()

    @BeforeTest
    fun setUp() {
        assertTrue(fs.exists(textPdf.path), "Missing fixture: ${textPdf.path} — run from project root")
        assertTrue(fs.exists(photoPdf.path), "Missing fixture: ${photoPdf.path} — run from project root")
        fs.createDirectories(tmpDir)
    }

    @AfterTest
    fun tearDown() {
        outputs.forEach { fs.delete(it, mustExist = false) }
        outputs.clear()
        fs.deleteRecursively(tmpDir, mustExist = false)
    }

    // ── text PDF — qpdf recompresses uncompressed streams ────────────────────

    @Test
    fun `HIGH_QUALITY - text PDF - qpdf only`() {
        compress(textPdf, CompressionProfile.HIGH_QUALITY)
            .let { reportAndAssert("HIGH_QUALITY text", it, textPdf) }
    }

    @Test
    fun `BALANCED - text PDF - qpdf then ghostscript`() {
        compress(textPdf, CompressionProfile.BALANCED)
            .let { reportAndAssert("BALANCED text", it, textPdf) }
    }

    @Test
    fun `MAXIMUM - text PDF - qpdf then ghostscript lowest DPI`() {
        compress(textPdf, CompressionProfile.MAXIMUM)
            .let { reportAndAssert("MAXIMUM text", it, textPdf) }
    }

    // ── photo PDF — ghostscript downsamples raster images ────────────────────

    @Test
    fun `HIGH_QUALITY - photo PDF - qpdf only`() {
        compress(photoPdf, CompressionProfile.HIGH_QUALITY)
            .let { reportAndAssert("HIGH_QUALITY photo", it, photoPdf) }
    }

    @Test
    fun `BALANCED - photo PDF - ghostscript downsamples to 144 DPI`() {
        compress(photoPdf, CompressionProfile.BALANCED)
            .let { reportAndAssert("BALANCED photo", it, photoPdf) }
    }

    @Test
    fun `MAXIMUM - photo PDF - ghostscript downsamples to 96 DPI`() {
        compress(photoPdf, CompressionProfile.MAXIMUM)
            .let { reportAndAssert("MAXIMUM photo", it, photoPdf) }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun compress(
        fixture: Fixture,
        profile: CompressionProfile,
    ): CompressionResult {
        val output = (tmpDir / "out_${fixture.path.name}_${profile.name.lowercase()}.pdf").also { outputs += it }
        var result: CompressionResult? = null
        val elapsed =
            measureTime {
                result =
                    PdfCompressionService().compress(
                        CompressionRequest(inputPath = fixture.path, outputPath = output, profile = profile),
                    )
            }
        println("[integration] ${profile.name} ${fixture.path.name} wall-clock: $elapsed")
        return result!!
    }

    private fun reportAndAssert(
        label: String,
        result: CompressionResult,
        fixture: Fixture,
    ) {
        assertIs<CompressionResult.Success>(result, "Expected success, got: $result")
        println(
            "[integration] $label: ${result.originalBytes / 1024}KB → ${result.compressedBytes / 1024}KB (${result.savedPercent}% saved)",
        )
        assertTrue(fs.exists(result.outputPath), "Output file must exist at ${result.outputPath}")
        assertEquals(
            result.originalBytes,
            fixture.originalBytes,
            "$label fixture size changed: expected ${fixture.originalBytes}B but got ${result.originalBytes}B. " +
                "Update the expected constant if the fixture was intentionally replaced.",
        )
        val range =
            when (result.profile) {
                CompressionProfile.HIGH_QUALITY -> fixture.highQualityRange
                CompressionProfile.BALANCED -> fixture.balancedRange
                CompressionProfile.MAXIMUM -> fixture.maximumRange
            }
        assertTrue(
            result.compressedBytes in range,
            "$label compressed size ${result.compressedBytes}B is outside expected range " +
                "${range.first}B..${range.last}B — fixture or tool version may have changed",
        )
    }
}
